package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.CambioEstadoPedidoDTO;
import redSelection.dulceMayor.dto.PedidoCrearDTO;
import redSelection.dulceMayor.dto.PedidoRespuestaDTO;
import redSelection.dulceMayor.enums.EstadoPedido;
import redSelection.dulceMayor.enums.EstadoStockPedido;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.patrones.strategy.promocion.ReglaPromocion.LineaPromocion;
import redSelection.dulceMayor.repository.ClienteMinoristaRepository;
import redSelection.dulceMayor.repository.PedidoRepository;
import redSelection.dulceMayor.repository.PreventistaRepository;
import redSelection.dulceMayor.repository.ResponsableComercioRepository;
import redSelection.dulceMayor.security.UsuarioActual;
import redSelection.dulceMayor.security.UsuarioAutenticado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Pedidos. PATRÓN: Template Method (capa Service) sobre BaseService:
 * - aEntidad arma el pedido según quién lo carga (preventista, administración o cliente) y aplica la promoción;
 * - despuesDeCrear lo factura automáticamente (BK-05) y registra la visita del preventista;
 * - buscarTodos y obtener filtran por dueño (cada uno ve lo suyo; administración ve todo);
 * - un pedido no se modifica ni se borra: se anula con un cambio de estado (patrón State en Pedido).
 */
@Service
public class PedidoService extends BaseService<Pedido, PedidoCrearDTO, PedidoRespuestaDTO> {

    private final PedidoRepository pedidoRepository;
    private final ClienteMinoristaRepository clienteRepository;
    private final ResponsableComercioRepository responsableRepository;
    private final PreventistaRepository preventistaRepository;
    private final ProductoService productoService;
    private final PrecioService precioService;
    private final PromocionService promocionService;
    private final FacturaClienteService facturaService;
    private final RutaService rutaService;
    private final EntregaService entregaService;
    private final UsuarioActual usuarioActual;

    public PedidoService(PedidoRepository pedidoRepository, ClienteMinoristaRepository clienteRepository,
                         ResponsableComercioRepository responsableRepository, PreventistaRepository preventistaRepository,
                         ProductoService productoService, PrecioService precioService,
                         PromocionService promocionService, FacturaClienteService facturaService,
                         RutaService rutaService, EntregaService entregaService, UsuarioActual usuarioActual) {
        super(pedidoRepository);
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.responsableRepository = responsableRepository;
        this.preventistaRepository = preventistaRepository;
        this.productoService = productoService;
        this.precioService = precioService;
        this.promocionService = promocionService;
        this.facturaService = facturaService;
        this.rutaService = rutaService;
        this.entregaService = entregaService;
        this.usuarioActual = usuarioActual;
    }

    @Override protected String nombreRecurso() { return "Pedido"; }

    // =====================================================
    // ALTA (pasos del Template Method)
    // =====================================================

    @Override
    protected Pedido aEntidad(PedidoCrearDTO dto) {
        UsuarioAutenticado usuario = usuarioActual.obtener();
        Preventista preventistaLogueado = preventistaDe(usuario);
        ClienteMinorista cliente = comercioDelPedido(dto, usuario, preventistaLogueado);
        Preventista asignado = preventistaLogueado != null ? preventistaLogueado : cliente.getPreventistaAsignado();
        if (asignado == null) {
            asignado = preventistaRepository.findByEliminadoFalse().stream().findFirst()
                    .orElseThrow(() -> new ReglaNegocioException("No hay un preventista asignado para procesar pedidos."));
        }

        Pedido pedido = Pedido.builder().fechaHoraCarga(LocalDateTime.now()).cliente(cliente)
                .preventista(asignado).estado(EstadoPedido.CARGADO).estadoStock(EstadoStockPedido.PENDIENTE)
                .subtotal(BigDecimal.ZERO).descuento(BigDecimal.ZERO).total(BigDecimal.ZERO)
                .items(new ArrayList<>()).build();
        // El stock no se controla acá: la facturación decide si se factura o queda esperando stock (BK-05)
        BigDecimal subtotal = BigDecimal.ZERO;
        for (PedidoCrearDTO.Linea linea : dto.items()) {
            Producto producto = productoService.obtenerEntidad(linea.productId());
            BigDecimal precio = precioService.precioVigenteObligatorio(producto, LocalDate.now());
            BigDecimal importe = precio.multiply(BigDecimal.valueOf(linea.quantity()));
            pedido.getItems().add(PedidoItem.builder().pedido(pedido).producto(producto)
                    .cantidad(linea.quantity()).precioUnitario(precio).subtotal(importe).build());
            subtotal = subtotal.add(importe);
        }
        pedido.setSubtotal(subtotal);

        BigDecimal descuento = BigDecimal.ZERO;
        if (dto.promoCode() != null && !dto.promoCode().isBlank()) {
            // BK-06: un código inexistente, vencido o que no aplica a este pedido se rechaza (422) con el motivo
            Promocion promocion = promocionService.vigentePorCodigo(dto.promoCode());
            descuento = promocionService.calcularDescuento(promocion, pedido.getItems().stream()
                    .map(i -> new LineaPromocion(i.getProducto().getId(), i.getCantidad(), i.getSubtotal()))
                    .toList());
            pedido.setPromocion(promocion);
        }
        pedido.setDescuento(descuento);
        pedido.setTotal(subtotal.subtract(descuento));
        pedido.registrarAlta(usuario.getNombreUsuario());
        return pedido;
    }

    /** Ya guardado: se intenta facturar solo (BK-05) y, si lo cargó un preventista, cuenta como visita. */
    @Override
    protected void despuesDeCrear(Pedido pedido, PedidoCrearDTO dto) {
        UsuarioAutenticado usuario = usuarioActual.obtener();
        facturaService.facturarAlCargar(pedido, usuario.getNombreUsuario());
        if (usuario.getRol() == Rol.PREVENTISTA) {
            rutaService.registrarVisitaPorPedido(pedido.getPreventista().getId(), pedido.getCliente().getId());
        }
    }

    /** Comercio del pedido: el preventista elige uno de SUS puntos de venta; administración, cualquiera; el cliente, el suyo. */
    private ClienteMinorista comercioDelPedido(PedidoCrearDTO dto, UsuarioAutenticado usuario, Preventista preventista) {
        if (preventista != null) {
            String puntoId = dto.pointOfSaleId() == null ? "" : dto.pointOfSaleId().trim();
            return clienteRepository.findByIdAndEliminadoFalse(puntoId)
                    .filter(c -> c.getPreventistaAsignado() != null
                            && c.getPreventistaAsignado().getId().equals(preventista.getId()))
                    .orElseThrow(() -> new RecursoNoEncontradoException("No encontramos ese punto de venta entre los tuyos."));
        }
        if (usuario.getRol().esAdministracion()) {
            String nombre = dto.clientName() == null ? "" : dto.clientName().trim();
            return clienteRepository.findByEliminadoFalse().stream()
                    .filter(c -> c.getNombreComercio().equalsIgnoreCase(nombre) || c.getRazonSocial().equalsIgnoreCase(nombre))
                    .findFirst()
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe un comercio con ese nombre."));
        }
        ResponsableComercio responsable = responsableDe(usuario);
        return (responsable == null ? List.<ClienteMinorista>of()
                : clienteRepository.findByResponsableIdAndEliminadoFalse(responsable.getId())).stream().findFirst()
                .orElseThrow(() -> new ReglaNegocioException(
                        "Tu cuenta todavía no está vinculada a un comercio. Contactá a un administrador para asociarla."));
    }

    // =====================================================
    // LECTURAS: cada uno ve lo suyo
    // =====================================================

    @Override
    protected List<Pedido> buscarTodos() {
        UsuarioAutenticado usuario = usuarioActual.obtener();
        if (usuario.getRol().esAdministracion()) {
            return super.buscarTodos();
        }
        Preventista preventista = preventistaDe(usuario);
        if (preventista != null) {
            return sinBorrados(pedidoRepository.findByPreventistaIdOrderByFechaHoraCargaDesc(preventista.getId()));
        }
        ResponsableComercio responsable = responsableDe(usuario);
        return responsable == null ? List.of()
                : sinBorrados(pedidoRepository.findByClienteResponsableIdOrderByFechaHoraCargaDesc(responsable.getId()));
    }

    /** Un pedido ajeno responde 404 y no 403: no se confirma que exista. */
    @Override
    protected Pedido buscarEntidad(String id) {
        Pedido pedido = super.buscarEntidad(id);
        UsuarioAutenticado usuario = usuarioActual.obtener();
        if (!usuario.getRol().esAdministracion() && !esDueno(pedido, usuario)) {
            throw new RecursoNoEncontradoException(nombreRecurso(), id);
        }
        return pedido;
    }

    // =====================================================
    // MODIFICACIÓN Y BAJA: no se permiten
    // =====================================================

    @Override
    protected void validarActualizacion(Pedido pedido, PedidoCrearDTO dto) {
        throw new ReglaNegocioException("Un pedido cargado no se modifica: se anula y se carga uno nuevo.");
    }

    @Override
    protected void aplicarCambios(Pedido pedido, PedidoCrearDTO dto) {
        // Nunca se llega acá: validarActualizacion lo impide
    }

    @Override
    protected void validarBaja(Pedido pedido) {
        throw new ReglaNegocioException("Un pedido no se borra: se anula (cambio de estado a ANULADO) para conservar su historial.");
    }

    // =====================================================
    // CAMBIO DE ESTADO (administración)
    // =====================================================

    /**
     * El estado actual (patrón State) valida la transición y la registra en el historial. Facturar emite la factura y
     * descuenta el stock; anular un pedido facturado anula la factura y devuelve el stock. Si algo falla, la
     * transacción se revierte y el pedido queda como estaba.
     */
    @Transactional
    public PedidoRespuestaDTO cambiarEstado(String id, CambioEstadoPedidoDTO dto) {
        Pedido pedido = pedidoRepository.buscarConBloqueo(id).filter(p -> !p.isEliminado())
                .orElseThrow(() -> new RecursoNoEncontradoException(nombreRecurso(), id));
        EstadoPedido destino;
        try {
            destino = EstadoPedido.valueOf(dto.status().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException("El estado solicitado no es válido: " + dto.status() + ".");
        }
        if (destino == EstadoPedido.EN_REPARTO) {
            // Despachar es asignar a un transportista (crea la Entrega y la parada): solo por DespachoService.
            throw new ReglaNegocioException("Para despachar un pedido hay que asignarlo a un transportista (Despacho).");
        }
        String usuario = usuarioActual.nombreOSistema();
        if (destino == EstadoPedido.FACTURADO) {
            facturaService.facturarPedido(pedido, usuario);
        } else {
            pedido.avanzarA(destino, usuario, dto.note());
            if (destino == EstadoPedido.ANULADO) {
                facturaService.anularFacturaDe(pedido);
            }
        }
        return aRespuesta(pedidoRepository.save(pedido));
    }

    // =====================================================
    // DESPACHO (lo usa DespachoService)
    // =====================================================

    /**
     * Pasa pedidos FACTURADOS a EN_REPARTO (el State valida la transición y la deja en el historial). Se toma cada pedido
     * con bloqueo, en orden de id, para que dos administradores no despachen el mismo pedido a la vez. Todos o ninguno:
     * si uno no se puede despachar, la transacción se revierte. Un comercio no puede repetirse: una parada lleva un
     * solo pedido.
     */
    @Transactional
    public List<Pedido> despacharFacturados(Collection<String> ids, String observacion) {
        List<Pedido> pedidos = new ArrayList<>();
        for (String id : new TreeSet<>(ids)) {
            pedidos.add(pedidoRepository.buscarConBloqueo(id).filter(p -> !p.isEliminado())
                    .orElseThrow(() -> new RecursoNoEncontradoException(nombreRecurso(), id)));
        }
        validarDespacho(pedidos);
        String usuario = usuarioActual.nombreOSistema();
        pedidos.forEach(pedido -> pedido.despachar(usuario, observacion));
        return pedidoRepository.saveAll(pedidos);
    }

    private void validarDespacho(List<Pedido> pedidos) {
        Map<String, Integer> porComercio = new LinkedHashMap<>();
        for (Pedido pedido : pedidos) {
            String comercio = pedido.getCliente().getNombreComercio();
            if (pedido.getEstado() != EstadoPedido.FACTURADO) {
                throw new ReglaNegocioException("El pedido de " + comercio + " no se puede despachar: está "
                        + pedido.getEstado() + " y solo se despachan pedidos FACTURADOS.");
            }
            porComercio.merge(pedido.getCliente().getId() + "|" + comercio, 1, Integer::sum);
        }
        porComercio.forEach((clave, cantidad) -> {
            if (cantidad > 1) {
                throw new ReglaNegocioException(clave.substring(clave.indexOf('|') + 1) + " tiene " + cantidad
                        + " pedidos en este despacho: asignalos en despachos separados.");
            }
        });
    }

    // =====================================================
    // RESPUESTA (DTO)
    // =====================================================

    @Override
    protected PedidoRespuestaDTO aRespuesta(Pedido pedido) {
        List<PedidoRespuestaDTO.Linea> lineas = pedido.getItems().stream()
                .map(i -> new PedidoRespuestaDTO.Linea(i.getProducto().getId(), i.getProducto().getNombre(),
                        i.getCantidad(), i.getPrecioUnitario(), i.getSubtotal()))
                .toList();
        FacturaCliente factura = facturaService.buscarPorPedido(pedido.getId()).orElse(null);
        return new PedidoRespuestaDTO(pedido.getId(), pedido.getCliente().getNombreComercio(),
                pedido.getEstado().name(), "A_COORDINAR", pedido.getFechaHoraCarga().toString(),
                factura == null ? null : factura.getFechaFactura().toString(),
                factura == null ? null : factura.getId(),
                pedido.getTotal(), lineas,
                new PedidoRespuestaDTO.Traza(eventos(pedido), transportista(pedido)),
                pedido.getDescuento(), puntoDeVenta(pedido.getCliente()));
    }

    /** Historial de estados con la forma que usa el frontend ({ state, at }). */
    private List<PedidoRespuestaDTO.Evento> eventos(Pedido pedido) {
        if (pedido.getHistorial().isEmpty()) {
            // Pedidos creados antes de BK-04: solo se conoce la fecha de carga
            return List.of(new PedidoRespuestaDTO.Evento(EstadoPedido.CARGADO.name(),
                    pedido.getFechaHoraCarga().toString(), null, null));
        }
        return pedido.getHistorial().stream()
                .map(h -> new PedidoRespuestaDTO.Evento(h.getEstadoNuevo().name(), h.getFechaHora().toString(),
                        h.getUsuario(), h.getObservacion()))
                .toList();
    }

    /** Datos del transportista (trazabilidad que pide el enunciado). Null hasta que el pedido sale a reparto. */
    private PedidoRespuestaDTO.Transportista transportista(Pedido pedido) {
        return entregaService.buscarPorPedido(pedido.getId())
                .map(Entrega::getTransportista)
                .map(t -> new PedidoRespuestaDTO.Transportista(t.getNombre() + " " + t.getApellido(),
                        t.getPatenteVehiculo() == null ? "" : "Patente " + t.getPatenteVehiculo()))
                .orElse(null);
    }

    private PedidoRespuestaDTO.PuntoDeVenta puntoDeVenta(ClienteMinorista cliente) {
        Direccion d = cliente.getDireccion();
        String direccion = d == null ? "" : d.getCalle() + " " + d.getNumeracion() + ", " + d.getLocalidadNombre();
        return new PedidoRespuestaDTO.PuntoDeVenta(cliente.getId(), cliente.getNombreComercio(), direccion,
                d == null ? null : d.getLatitud(), d == null ? null : d.getLongitud());
    }

    // =====================================================
    // AUXILIARES
    // =====================================================

    /** El preventista del usuario logueado, o null si no es preventista. */
    private Preventista preventistaDe(UsuarioAutenticado usuario) {
        if (usuario.getRol() != Rol.PREVENTISTA) {
            return null;
        }
        if (usuario.getEmpleadoId() == null) {
            throw new ReglaNegocioException("Tu usuario no está asociado a un preventista.");
        }
        return preventistaRepository.findByIdAndEliminadoFalse(usuario.getEmpleadoId())
                .orElseThrow(() -> new ReglaNegocioException("Tu usuario no está asociado a un preventista."));
    }

    private ResponsableComercio responsableDe(UsuarioAutenticado usuario) {
        return responsableRepository.findByEliminadoFalse().stream()
                .filter(r -> r.getUsuario() != null
                        && r.getUsuario().getNombreUsuario().equalsIgnoreCase(usuario.getNombreUsuario()))
                .findFirst().orElse(null);
    }

    private boolean esDueno(Pedido pedido, UsuarioAutenticado usuario) {
        if (usuario.getRol() == Rol.PREVENTISTA) {
            return pedido.getPreventista().getId().equals(usuario.getEmpleadoId());
        }
        ResponsableComercio responsable = responsableDe(usuario);
        return responsable != null && pedido.getCliente().getResponsable() != null
                && pedido.getCliente().getResponsable().getId().equals(responsable.getId());
    }

    private static List<Pedido> sinBorrados(List<Pedido> pedidos) {
        return pedidos.stream().filter(p -> !p.isEliminado()).toList();
    }
}
