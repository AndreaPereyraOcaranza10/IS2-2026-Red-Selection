package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.enums.EstadoFactura;
import redSelection.dulceMayor.enums.EstadoStockPedido;
import redSelection.dulceMayor.enums.TipoMovimientoStock;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.repository.FacturaClienteRepository;
import redSelection.dulceMayor.security.UsuarioAutenticado;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Facturación de pedidos (BK-05) y acceso a facturas para otros servicios (PagoService, EntregaService).
 *
 * Al cargar un pedido se intenta facturar solo: si hay stock de todas las líneas se descuenta, se emite la factura
 * con número correlativo y el pedido pasa a FACTURADO; si falta stock queda en PENDIENTE_STOCK hasta que
 * administración vuelva a facturarlo. Los cambios de estado los valida el pedido (patrón State).
 */
@Service
public class FacturaClienteService {

    private final FacturaClienteRepository facturaRepository;
    private final ProductoService productoService;
    private final StockService stockService;

    public FacturaClienteService(FacturaClienteRepository facturaRepository, ProductoService productoService,
                                 StockService stockService) {
        this.facturaRepository = facturaRepository;
        this.productoService = productoService;
        this.stockService = stockService;
    }

    // =====================================================
    // CONSULTAS
    // =====================================================

    @Transactional(readOnly = true)
    public FacturaCliente obtenerEntidad(String id) {
        return facturaRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura", id));
    }

    /** La factura de un pedido, si ya fue emitida (el pedido puede no estar facturado todavía). */
    @Transactional(readOnly = true)
    public Optional<FacturaCliente> buscarPorPedido(String pedidoId) {
        return facturaRepository.findByPedidoId(pedidoId).filter(f -> !f.isEliminado());
    }

    /**
     * Factura para descargar: administración ve cualquiera; el preventista, las de sus pedidos; el cliente, las de
     * su comercio. Una ajena responde 404 (no se confirma que exista).
     */
    @Transactional(readOnly = true)
    public FacturaCliente obtenerParaDescarga(String id, UsuarioAutenticado usuario) {
        FacturaCliente factura = obtenerEntidad(id);
        Pedido pedido = factura.getPedido();
        boolean puede = usuario.getRol().esAdministracion() || switch (usuario.getRol()) {
            case PREVENTISTA -> pedido.getPreventista().getId().equals(usuario.getEmpleadoId());
            case CLIENTE -> {
                ResponsableComercio r = pedido.getCliente().getResponsable();
                yield r != null && r.getUsuario() != null
                        && r.getUsuario().getNombreUsuario().equalsIgnoreCase(usuario.getNombreUsuario());
            }
            default -> false;
        };
        if (!puede) {
            throw new RecursoNoEncontradoException("Factura", id);
        }
        return factura;
    }

    // =====================================================
    // FACTURACIÓN
    // =====================================================

    /** Al cargar el pedido: lo factura si hay stock; si no, lo deja en PENDIENTE_STOCK con el detalle de lo que falta. */
    @Transactional
    public Optional<FacturaCliente> facturarAlCargar(Pedido pedido, String usuario) {
        Map<String, Producto> productos = bloquearProductos(pedido);
        List<String> faltantes = faltantes(pedido, productos);
        if (!faltantes.isEmpty()) {
            pedido.setEstadoStock(EstadoStockPedido.INSUFICIENTE);
            pedido.quedarPendienteDeStock(usuario, "Falta stock de " + String.join(", ", faltantes));
            return Optional.empty();
        }
        return Optional.of(emitir(pedido, usuario, productos));
    }

    /**
     * Facturación pedida por administración (un pedido CARGADO o que esperaba stock). Si ya tiene factura la
     * devuelve; si todavía falta stock, lo rechaza (422) diciendo qué falta.
     */
    @Transactional
    public FacturaCliente facturarPedido(Pedido pedido, String usuario) {
        Optional<FacturaCliente> existente = buscarPorPedido(pedido.getId());
        if (existente.isPresent()) {
            return existente.get();
        }
        pedido.estadoActual().facturar();   // valida la transición antes de tocar el stock (lanza si no corresponde)
        Map<String, Producto> productos = bloquearProductos(pedido);
        List<String> faltantes = faltantes(pedido, productos);
        if (!faltantes.isEmpty()) {
            throw new ReglaNegocioException("Todavía falta stock de " + String.join(", ", faltantes) + ".");
        }
        return emitir(pedido, usuario, productos);
    }

    /** Al anular un pedido facturado: la factura queda ANULADA y el stock vuelve. Con un pago confirmado no se puede. */
    @Transactional
    public void anularFacturaDe(Pedido pedido) {
        Optional<FacturaCliente> encontrada = buscarPorPedido(pedido.getId());
        if (encontrada.isEmpty()) {
            return;
        }
        FacturaCliente factura = encontrada.get();
        if (factura.getEstado() == EstadoFactura.PAGADA) {
            throw new ReglaNegocioException("La factura N° " + factura.getNumeroFactura()
                    + " ya está pagada: el pedido no se puede anular.");
        }
        if (factura.getEstado() == EstadoFactura.ANULADA) {
            return;
        }
        factura.setEstado(EstadoFactura.ANULADA);
        for (DetalleFactura detalle : factura.getDetalles()) {
            stockService.registrar(detalle.getProducto(), TipoMovimientoStock.REVERSION, detalle.getCantidad(), detalle);
        }
        facturaRepository.save(factura);
    }

    // =====================================================
    // AUXILIARES
    // =====================================================

    /** Bloquea cada producto del pedido (en orden por id, para no trabarse con otro pedido que use los mismos). */
    private Map<String, Producto> bloquearProductos(Pedido pedido) {
        Map<String, Producto> productos = new TreeMap<>();
        pedido.getItems().stream().map(i -> i.getProducto().getId()).sorted().distinct()
                .forEach(id -> productos.put(id, productoService.obtenerConBloqueo(id)));
        return productos;
    }

    /** "Alfajor (faltan 3)" por cada producto sin stock suficiente. Suma las líneas repetidas del mismo producto. */
    private List<String> faltantes(Pedido pedido, Map<String, Producto> productos) {
        Map<String, Integer> pedidas = pedido.getItems().stream().collect(Collectors.groupingBy(
                i -> i.getProducto().getId(), TreeMap::new, Collectors.summingInt(PedidoItem::getCantidad)));
        List<String> faltantes = new ArrayList<>();
        pedidas.forEach((id, cantidad) -> {
            long disponible = stockService.stockActual(id);
            if (disponible < cantidad) {
                faltantes.add(productos.get(id).getNombre() + " (faltan " + (cantidad - disponible) + ")");
            }
        });
        return faltantes;
    }

    /** Emite la factura correlativa, descuenta el stock de cada línea y pasa el pedido a FACTURADO. */
    private FacturaCliente emitir(Pedido pedido, String usuario, Map<String, Producto> productos) {
        long numero = facturaRepository.ultimoNumero() + 1;
        FacturaCliente factura = FacturaCliente.builder()
                .numeroFactura(numero)
                .fechaFactura(LocalDateTime.now())
                .estado(EstadoFactura.EMITIDA)
                .subtotal(pedido.getSubtotal()).descuento(pedido.getDescuento()).total(pedido.getTotal())
                .pedido(pedido)
                .detalles(new ArrayList<>())
                .build();
        for (PedidoItem item : pedido.getItems()) {
            factura.getDetalles().add(DetalleFactura.builder().factura(factura)
                    .producto(productos.get(item.getProducto().getId()))
                    .cantidad(item.getCantidad()).precioUnitario(item.getPrecioUnitario())
                    .subtotal(item.getSubtotal()).build());
        }
        // Primero se guarda la factura (con sus detalles) y después el stock, que referencia a cada detalle
        FacturaCliente guardada = facturaRepository.save(factura);
        for (DetalleFactura detalle : guardada.getDetalles()) {
            stockService.registrar(detalle.getProducto(), TipoMovimientoStock.VENTA, -detalle.getCantidad(), detalle);
        }
        pedido.setEstadoStock(EstadoStockPedido.CONFIRMADO);
        pedido.facturar(usuario, "Factura N° " + numero);
        return guardada;
    }
}
