package redSelection.dulceMayor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.CambioEstadoPedidoDTO;
import redSelection.dulceMayor.dto.PagoCrearDTO;
import redSelection.dulceMayor.dto.PagoRespuestaDTO;
import redSelection.dulceMayor.dto.PedidoCrearDTO;
import redSelection.dulceMayor.dto.PedidoRespuestaDTO;
import redSelection.dulceMayor.enums.EstadoPago;
import redSelection.dulceMayor.enums.EstadoPedido;
import redSelection.dulceMayor.enums.EstadoStockPedido;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.patrones.strategy.promocion.ReglaPromocion.LineaPromocion;
import redSelection.dulceMayor.repository.PedidoRepository;
import redSelection.dulceMayor.security.UsuarioActual;
import redSelection.dulceMayor.security.UsuarioAutenticado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pedidos. PATRÓN: Template Method (capa Service) sobre BaseService:
 * - aEntidad arma el pedido según quién lo carga (preventista, administración o cliente) y aplica la promoción;
 * - despuesDeCrear RESERVA el stock (el pedido queda PENDIENTE_PAGO) o lo deja esperando stock, y registra la visita
 *   del preventista; la factura se emite recién cuando el pago se acredita (PagoService) o al despachar en efectivo;
 * - buscarTodos y obtener filtran por dueño (cada uno ve lo suyo; administración ve todo);
 * - un pedido no se modifica ni se borra: se anula con un cambio de estado (patrón State en Pedido).
 */
@Service
public class PedidoService extends BaseService<Pedido, PedidoCrearDTO, PedidoRespuestaDTO> {

    private final PedidoRepository pedidoRepository;
    private final PuntoVentaService puntosVenta;              // los comercios (ClienteMinorista) son de PuntoVentaService
    private final ResponsableComercioService responsableService;
    private final PreventistaService preventistaService;
    private final ProductoService productoService;
    private final PrecioService precioService;
    private final PromocionService promocionService;
    private final FacturaClienteService facturaService;
    private final PagoService pagoService;
    private final StockService stockService;
    private final RutaService rutaService;
    private final EntregaService entregaService;
    private final UsuarioActual usuarioActual;
    private final long horasVencimientoPago;

    public PedidoService(PedidoRepository pedidoRepository, PuntoVentaService puntosVenta,
                         ResponsableComercioService responsableService, PreventistaService preventistaService,
                         ProductoService productoService, PrecioService precioService,
                         PromocionService promocionService, FacturaClienteService facturaService,
                         PagoService pagoService, StockService stockService,
                         RutaService rutaService, EntregaService entregaService, UsuarioActual usuarioActual,
                         @Value("${app.pedidos.horas-vencimiento-pago:48}") long horasVencimientoPago) {
        super(pedidoRepository);
        this.pedidoRepository = pedidoRepository;
        this.puntosVenta = puntosVenta;
        this.responsableService = responsableService;
        this.preventistaService = preventistaService;
        this.productoService = productoService;
        this.precioService = precioService;
        this.promocionService = promocionService;
        this.facturaService = facturaService;
        this.pagoService = pagoService;
        this.stockService = stockService;
        this.rutaService = rutaService;
        this.entregaService = entregaService;
        this.usuarioActual = usuarioActual;
        this.horasVencimientoPago = horasVencimientoPago;
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
            asignado = preventistaService.primerActivo()
                    .orElseThrow(() -> new ReglaNegocioException("No hay un preventista asignado para procesar pedidos."));
        }

        Pedido pedido = Pedido.builder().fechaHoraCarga(LocalDateTime.now()).cliente(cliente)
                .preventista(asignado).estado(EstadoPedido.CARGADO).estadoStock(EstadoStockPedido.PENDIENTE)
                .subtotal(BigDecimal.ZERO).descuento(BigDecimal.ZERO).total(BigDecimal.ZERO)
                .items(new ArrayList<>()).build();
        // El stock no se controla acá: despuesDeCrear decide si lo reserva o el pedido queda esperando stock
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

    /**
     * Ya guardado: se reserva el stock (el pedido espera el pago) o queda esperando stock; y, si lo cargó un
     * preventista, cuenta como visita. NO se factura: la factura sale cuando el pago se acredita.
     */
    @Override
    protected void despuesDeCrear(Pedido pedido, PedidoCrearDTO dto) {
        UsuarioAutenticado usuario = usuarioActual.obtener();
        reservarOEsperarStock(pedido, usuario.getNombreUsuario());
        if (usuario.getRol() == Rol.PREVENTISTA) {
            rutaService.registrarVisitaPorPedido(pedido.getPreventista().getId(), pedido.getCliente().getId());
        }
    }

    /** Comercio del pedido: el preventista elige uno de SUS puntos de venta; administración, cualquiera; el cliente, el suyo. */
    private ClienteMinorista comercioDelPedido(PedidoCrearDTO dto, UsuarioAutenticado usuario, Preventista preventista) {
        if (preventista != null) {
            String puntoId = dto.pointOfSaleId() == null ? "" : dto.pointOfSaleId().trim();
            return puntosVenta.buscarDelPreventista(puntoId, preventista.getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No encontramos ese punto de venta entre los tuyos."));
        }
        if (usuario.getRol().esAdministracion()) {
            String nombre = dto.clientName() == null ? "" : dto.clientName().trim();
            return puntosVenta.buscarPorNombre(nombre)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe un comercio con ese nombre."));
        }
        ResponsableComercio responsable = responsableDe(usuario);
        return (responsable == null ? Optional.<ClienteMinorista>empty() : puntosVenta.delResponsable(responsable.getId()))
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
     * El estado actual (patrón State) valida la transición y la registra en el historial. Pasar de PENDIENTE_STOCK a
     * PENDIENTE_PAGO reserva el stock cuando ya hay; anular libera la reserva, anula los pagos pendientes y, si estaba
     * facturado, anula la factura y devuelve el stock. La factura no se pide por acá: se emite al acreditarse el pago o
     * al despachar en efectivo. Si algo falla, la transacción se revierte y el pedido queda como estaba.
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
        if (destino == EstadoPedido.FACTURADO) {
            throw new ReglaNegocioException("La factura se emite sola cuando se acredita el pago (registrá el pago) "
                    + "o al despachar un pedido que se cobra en efectivo.");
        }
        String usuario = usuarioActual.nombreOSistema();
        if (destino == EstadoPedido.PENDIENTE_PAGO) {
            // Reintento de un pedido que esperaba stock: si ya alcanza, se reserva
            pedido.estadoActual().esperarPago();   // valida la transición antes de mirar el stock
            List<String> faltantes = faltantesParaReservar(pedido);
            if (!faltantes.isEmpty()) {
                throw new ReglaNegocioException("Todavía falta stock de " + String.join(", ", faltantes) + ".");
            }
            pedido.setEstadoStock(EstadoStockPedido.RESERVADO);
            pedido.esperarPago(usuario, dto.note() != null ? dto.note() : "Stock reservado. Esperando el pago.");
        } else {
            pedido.avanzarA(destino, usuario, dto.note());
            if (destino == EstadoPedido.ANULADO) {
                liberarAlAnular(pedido);
            }
        }
        return aRespuesta(pedidoRepository.save(pedido));
    }

    /** El cliente cancela su propio pedido mientras todavía no está facturado (sin stock o esperando el pago). */
    @Transactional
    public PedidoRespuestaDTO cancelar(String id) {
        Pedido pedido = pedidoRepository.buscarConBloqueo(id).filter(p -> !p.isEliminado())
                .orElseThrow(() -> new RecursoNoEncontradoException(nombreRecurso(), id));
        UsuarioAutenticado usuario = usuarioActual.obtener();
        if (!usuario.getRol().esAdministracion() && !esDueno(pedido, usuario)) {
            throw new RecursoNoEncontradoException(nombreRecurso(), id);   // ajeno: 404
        }
        if (!usuario.getRol().esAdministracion() && pedido.getEstado() != EstadoPedido.PENDIENTE_STOCK
                && pedido.getEstado() != EstadoPedido.PENDIENTE_PAGO) {
            throw new ReglaNegocioException("Este pedido ya está " + pedido.getEstado()
                    + ": para anularlo contactá a administración.");
        }
        pedido.anular(usuario.getNombreUsuario(), "Cancelado por " + usuario.getNombreUsuario());
        liberarAlAnular(pedido);
        return aRespuesta(pedidoRepository.save(pedido));
    }

    /** Anular libera la reserva (el pedido deja de estar en PENDIENTE_PAGO), cancela los pagos pendientes y revierte la factura. */
    private void liberarAlAnular(Pedido pedido) {
        pagoService.anularPendientesDe(pedido.getId());
        facturaService.anularFacturaDe(pedido);
    }

    // =====================================================
    // PAGO (BK-15)
    // =====================================================

    /**
     * Elige el medio de pago de un pedido que espera el pago. El cliente solo puede pagar con billetera virtual
     * (Mercado Pago); transferencia y efectivo los acuerda administración con el cliente.
     */
    @Transactional
    public PagoRespuestaDTO iniciarPago(String pedidoId, PagoCrearDTO dto) {
        Pedido pedido = buscarEntidad(pedidoId);   // un pedido ajeno responde 404
        UsuarioAutenticado usuario = usuarioActual.obtener();
        if (usuario.getRol() == Rol.CLIENTE && dto.tipoPago() != TipoPago.BILLETERA_VIRTUAL) {
            throw new AccessDeniedException("Como cliente podés pagar con billetera virtual. "
                    + "Para transferencia o efectivo contactá a administración.");
        }
        return pagoService.iniciar(pedido, dto.tipoPago());
    }

    /** Administración registra el cobro (ej.: la transferencia se acreditó): se emite la factura y el pedido pasa a FACTURADO. */
    @Transactional
    public PagoRespuestaDTO registrarPago(String pedidoId, PagoCrearDTO dto) {
        Pedido pedido = pedidoRepository.buscarConBloqueo(pedidoId).filter(p -> !p.isEliminado())
                .orElseThrow(() -> new RecursoNoEncontradoException(nombreRecurso(), pedidoId));
        return pagoService.confirmarPedido(pedido, dto.tipoPago());
    }

    /** Consulta a la pasarela si el cliente ya pagó con billetera virtual; si sí, el pago se confirma y se factura. */
    @Transactional
    public Optional<PagoRespuestaDTO> verificarPago(String pedidoId) {
        return pagoService.verificarPorPedido(buscarEntidad(pedidoId).getId());   // un pedido ajeno responde 404
    }

    /** Modo demo (pasarela simulada): equivale a pagar el QR desde la billetera. Con la pasarela real responde error. */
    @Transactional
    public Optional<PagoRespuestaDTO> simularAcreditacion(String pedidoId) {
        return pagoService.simularAcreditacion(buscarEntidad(pedidoId).getId());
    }

    /** El pago vigente del pedido, si lo hay (con las instrucciones y el QR para que el cliente pague). */
    @Transactional(readOnly = true)
    public Optional<PagoRespuestaDTO> pagoVigente(String pedidoId) {
        return pagoService.pagoVigentePorPedido(buscarEntidad(pedidoId).getId());
    }

    // =====================================================
    // RESERVA DE STOCK
    // =====================================================

    /** Unidades de un producto apartadas por pedidos que esperan el pago. Disponible = stock físico - reservado. */
    @Transactional(readOnly = true)
    public long cantidadReservada(String productoId) {
        return pedidoRepository.cantidadEnEstado(productoId, EstadoPedido.PENDIENTE_PAGO);
    }

    /** Si el stock disponible alcanza para todas las líneas, lo reserva (PENDIENTE_PAGO); si no, el pedido espera stock. */
    private void reservarOEsperarStock(Pedido pedido, String usuario) {
        List<String> faltantes = faltantesParaReservar(pedido);
        if (faltantes.isEmpty()) {
            pedido.setEstadoStock(EstadoStockPedido.RESERVADO);
            pedido.esperarPago(usuario, "Stock reservado. Esperando el pago.");
        } else {
            pedido.setEstadoStock(EstadoStockPedido.INSUFICIENTE);
            pedido.quedarPendienteDeStock(usuario, "Falta stock de " + String.join(", ", faltantes));
        }
    }

    /**
     * "Alfajor (faltan 3)" por cada producto cuyo stock disponible no alcanza. Los productos se bloquean en orden de id
     * (TreeMap) para que dos pedidos simultáneos no reserven la misma mercadería ni se traben entre sí.
     */
    private List<String> faltantesParaReservar(Pedido pedido) {
        Map<String, Integer> pedidas = pedido.getItems().stream().collect(Collectors.groupingBy(
                i -> i.getProducto().getId(), TreeMap::new, Collectors.summingInt(PedidoItem::getCantidad)));
        List<String> faltantes = new ArrayList<>();
        pedidas.forEach((productoId, cantidad) -> {
            Producto producto = productoService.obtenerConBloqueo(productoId);
            long disponible = stockService.stockActual(productoId) - cantidadReservada(productoId);
            if (disponible < cantidad) {
                faltantes.add(producto.getNombre() + " (faltan " + (cantidad - Math.max(disponible, 0)) + ")");
            }
        });
        return faltantes;
    }

    // =====================================================
    // VENCIMIENTO (lo dispara VencimientoPedidosJob)
    // =====================================================

    /**
     * Anula los pedidos que llevan más de N horas esperando el pago, para no tener stock reservado de más. Los que
     * quedaron acordados en efectivo no vencen: se cobran al entregar. Devuelve cuántos anuló.
     */
    @Transactional
    public int vencerPedidosSinPago() {
        LocalDateTime limite = LocalDateTime.now().minusHours(horasVencimientoPago);
        int anulados = 0;
        for (Pedido candidato : pedidoRepository.findByEstadoAndEliminadoFalseAndFechaHoraCargaBefore(
                EstadoPedido.PENDIENTE_PAGO, limite)) {
            Pedido pedido = pedidoRepository.buscarConBloqueo(candidato.getId()).orElse(null);
            if (pedido == null || pedido.getEstado() != EstadoPedido.PENDIENTE_PAGO
                    || esperaPagoDesde(pedido).isAfter(limite)
                    || esEfectivoAcordado(pedido)) {
                continue;
            }
            pedido.anular("sistema", "Vencido: no se registró el pago en " + horasVencimientoPago + " horas.");
            liberarAlAnular(pedido);
            pedidoRepository.save(pedido);
            anulados++;
        }
        return anulados;
    }

    /** Desde cuándo espera el pago: el último cambio a PENDIENTE_PAGO del historial (un reintento por falta de stock lo reinicia). */
    private static LocalDateTime esperaPagoDesde(Pedido pedido) {
        return pedido.getHistorial().stream()
                .filter(h -> h.getEstadoNuevo() == EstadoPedido.PENDIENTE_PAGO)
                .map(HistorialEstadoPedido::getFechaHora)
                .max(Comparator.naturalOrder())
                .orElse(pedido.getFechaHoraCarga());
    }

    private boolean esEfectivoAcordado(Pedido pedido) {
        return pagoService.pagoVigentePorPedido(pedido.getId())
                .filter(p -> p.tipoPago() == TipoPago.EFECTIVO && p.estado() == EstadoPago.PENDIENTE).isPresent();
    }

    // =====================================================
    // DESPACHO (lo usa DespachoService)
    // =====================================================

    /**
     * Pasa pedidos a EN_REPARTO (el State valida la transición y la deja en el historial). Se despachan los FACTURADOS
     * (ya pagos) y los que esperan el pago pero se acordaron en EFECTIVO: a esos se les emite la factura recién ahora
     * (el cobro lo hace el transportista al entregar). Se toma cada pedido con bloqueo, en orden de id, para que dos
     * administradores no despachen el mismo pedido a la vez. Todos o ninguno: si uno no se puede despachar, la
     * transacción se revierte. Un comercio no puede repetirse: una parada lleva un solo pedido.
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
        for (Pedido pedido : pedidos) {
            if (pedido.getEstado() == EstadoPedido.PENDIENTE_PAGO) {
                // Efectivo: el stock ya estaba reservado, se factura ahora y el pago queda pendiente hasta la entrega
                FacturaCliente factura = facturaService.facturarPedido(pedido, usuario);
                pagoService.vincularFactura(pedido.getId(), factura);
            }
            pedido.despachar(usuario, observacion);
        }
        return pedidoRepository.saveAll(pedidos);
    }

    private void validarDespacho(List<Pedido> pedidos) {
        Map<String, Integer> porComercio = new LinkedHashMap<>();
        for (Pedido pedido : pedidos) {
            String comercio = pedido.getCliente().getNombreComercio();
            boolean efectivoAcordado = pedido.getEstado() == EstadoPedido.PENDIENTE_PAGO && esEfectivoAcordado(pedido);
            if (pedido.getEstado() != EstadoPedido.FACTURADO && !efectivoAcordado) {
                String motivo = pedido.getEstado() == EstadoPedido.PENDIENTE_PAGO
                        ? "todavía espera el pago (solo se despacha pago o acordado en efectivo)"
                        : "está " + pedido.getEstado() + " y solo se despachan pedidos pagos o acordados en efectivo";
                throw new ReglaNegocioException("El pedido de " + comercio + " no se puede despachar: " + motivo + ".");
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
                pedido.getEstado().name(),
                pagoService.pagoVigentePorPedido(pedido.getId()).map(pago -> pago.tipoPago().name()).orElse("A_COORDINAR"),
                pedido.getFechaHoraCarga().toString(),
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
        try {
            return preventistaService.obtenerEntidad(usuario.getEmpleadoId());
        } catch (RecursoNoEncontradoException e) {
            throw new ReglaNegocioException("Tu usuario no está asociado a un preventista.");
        }
    }

    private ResponsableComercio responsableDe(UsuarioAutenticado usuario) {
        return responsableService.buscarPorNombreUsuario(usuario.getNombreUsuario()).orElse(null);
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

    // ---------- Consultas agregadas para EstadisticasService y los reportes (nadie más lee PedidoRepository) ----------

    @Transactional(readOnly = true)
    public List<PedidoRepository.PedidosPorDia> pedidosPorPreventistaDia(
            java.time.LocalDateTime desde, java.time.LocalDateTime hasta, redSelection.dulceMayor.enums.EstadoPedido anulado) {
        return pedidoRepository.pedidosPorPreventistaDia(desde, hasta, anulado);
    }

    @Transactional(readOnly = true)
    public List<PedidoRepository.PedidosPorMes> pedidosPorPreventistaMes(
            java.time.LocalDateTime desde, java.time.LocalDateTime hasta, redSelection.dulceMayor.enums.EstadoPedido anulado) {
        return pedidoRepository.pedidosPorPreventistaMes(desde, hasta, anulado);
    }

    @Transactional(readOnly = true)
    public List<PedidoRepository.PedidosPorEstadoPreventista> pedidosPorEstadoPreventista(
            java.time.LocalDateTime desde, java.time.LocalDateTime hasta) {
        return pedidoRepository.pedidosPorEstadoPreventista(desde, hasta);
    }

    @Transactional(readOnly = true)
    public List<PedidoRepository.VentasPreventista> ventasPorPreventista(
            java.time.LocalDateTime desde, java.time.LocalDateTime hasta,
            List<redSelection.dulceMayor.enums.EstadoPedido> estadosFacturados) {
        return pedidoRepository.ventasPorPreventista(desde, hasta, estadosFacturados);
    }

    /** Para los reportes: pedidos activos cargados en el período, del más viejo al más nuevo. */
    @Transactional(readOnly = true)
    public List<Pedido> cargadosEntre(java.time.LocalDateTime desde, java.time.LocalDateTime hasta) {
        return pedidoRepository.findByEliminadoFalseAndFechaHoraCargaBetweenOrderByFechaHoraCargaAsc(desde, hasta);
    }

}