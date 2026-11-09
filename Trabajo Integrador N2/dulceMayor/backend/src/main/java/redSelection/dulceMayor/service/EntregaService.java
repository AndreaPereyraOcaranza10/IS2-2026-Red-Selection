package redSelection.dulceMayor.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.EntregaDetalleDTO;
import redSelection.dulceMayor.dto.EntregaResumenDTO;
import redSelection.dulceMayor.dto.ItemEntregaDTO;
import redSelection.dulceMayor.dto.PagoRespuestaDTO;
import redSelection.dulceMayor.enums.EstadoPedido;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.ClienteMinorista;
import redSelection.dulceMayor.model.Direccion;
import redSelection.dulceMayor.model.Entrega;
import redSelection.dulceMayor.model.FacturaCliente;
import redSelection.dulceMayor.model.Pedido;
import redSelection.dulceMayor.model.ResponsableComercio;
import redSelection.dulceMayor.model.Transportista;
import redSelection.dulceMayor.repository.EntregaRepository;
import redSelection.dulceMayor.security.UsuarioActual;
import redSelection.dulceMayor.security.UsuarioAutenticado;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Lo que el transportista consulta y hace sobre sus entregas: historial (TR-01), detalle con productos y medio
 * de pago (TR-01) y cobro en efectivo contra entrega (TR-02).
 * No extiende BaseService a propósito: no es un CRUD, son consultas y un caso de uso. La plata la mueve
 * PagoService (este service no toca pagos ni facturas ajenos por repository, solo por sus services).
 */
@Service
public class EntregaService {

    private static final int DIAS_POR_DEFECTO = 30;

    private final EntregaRepository entregaRepository;
    private final FacturaClienteService facturaService;
    private final PagoService pagoService;
    private final UsuarioActual usuarioActual;

    public EntregaService(EntregaRepository entregaRepository,
                          FacturaClienteService facturaService,
                          PagoService pagoService,
                          UsuarioActual usuarioActual) {
        this.entregaRepository = entregaRepository;
        this.facturaService = facturaService;
        this.pagoService = pagoService;
        this.usuarioActual = usuarioActual;
    }

    // ---------- TR-01: historial y detalle ----------

    /** Entregas realizadas por un transportista entre dos fechas (por defecto, los últimos 30 días). */
    @Transactional(readOnly = true)
    public List<EntregaResumenDTO> historial(String transportistaId, LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.minusDays(DIAS_POR_DEFECTO);
        if (inicio.isAfter(fin)) {
            throw new ReglaNegocioException("La fecha 'desde' no puede ser posterior a 'hasta'.");
        }
        return entregaRepository
                .findByTransportistaIdAndFechaEntregaBetweenAndEliminadoFalseOrderByFechaEntregaDesc(
                        transportistaId, inicio.atStartOfDay(), fin.atTime(LocalTime.MAX))
                .stream().map(this::aResumen).toList();
    }

    /** Detalle de la entrega de un pedido. La ve su transportista o la administración; nadie más. */
    @Transactional(readOnly = true)
    public EntregaDetalleDTO detalle(String pedidoId) {
        Entrega entrega = buscar(pedidoId);
        exigirAcceso(entrega);
        Pedido pedido = entrega.getPedido();
        Optional<FacturaCliente> factura = facturaService.buscarPorPedido(pedidoId);
        Optional<PagoRespuestaDTO> pago = factura.flatMap(f -> pagoService.pagoVigente(f.getId()));
        Transportista transportista = entrega.getTransportista();

        List<ItemEntregaDTO> items = pedido.getItems().stream()
                .filter(i -> !i.isEliminado())
                .map(i -> new ItemEntregaDTO(i.getProducto().getId(), i.getProducto().getNombre(),
                        i.getCantidad(), i.getPrecioUnitario(), i.getSubtotal()))
                .toList();
        ClienteMinorista cliente = pedido.getCliente();
        ResponsableComercio responsable = cliente.getResponsable();
        return new EntregaDetalleDTO(
                pedido.getId(),
                factura.map(FacturaCliente::getNumeroFactura).orElse(null),
                cliente.getNombreComercio(),
                responsable.getNombre() + " " + responsable.getApellido(),
                texto(cliente.getDireccion()),
                pedido.getEstado(),
                items,
                pedido.getSubtotal(), pedido.getDescuento(), pedido.getTotal(),
                pago.map(PagoRespuestaDTO::tipoPago).orElse(null),
                pago.map(PagoRespuestaDTO::estado).orElse(null),
                pago.map(PagoRespuestaDTO::instrucciones).orElse(null),
                pedido.getFechaHoraCarga(),
                factura.map(FacturaCliente::getFechaFactura).orElse(null),
                entrega.getFechaDespacho(),
                entrega.getFechaEntrega(),
                transportista == null ? null : transportista.getNombre() + " " + transportista.getApellido(),
                transportista == null ? null : transportista.getPatenteVehiculo());
    }

    // ---------- TR-02: cobro en efectivo contra entrega ----------

    /**
     * El transportista dueño de la entrega registra el cobro en efectivo. Reglas:
     * 1) solo el transportista asignado (no otro, ni siquiera administración por esta vía: ella usa /api/pagos);
     * 2) el pedido tiene que estar en reparto o ya entregado, y facturado;
     * 3) si se acordó otro medio (ej. transferencia), el transportista NO lo puede pisar;
     * 4) el monto sale de la factura y es idempotente: cobrar dos veces devuelve el mismo pago.
     */
    @Transactional
    public PagoRespuestaDTO cobrarEnEfectivo(String pedidoId) {
        Entrega entrega = buscar(pedidoId);
        exigirPropietario(entrega);
        EstadoPedido estado = entrega.getPedido().getEstado();
        if (estado != EstadoPedido.EN_REPARTO && estado != EstadoPedido.ENTREGADO) {
            throw new ReglaNegocioException("Solo se cobra un pedido que está en reparto o ya entregado (estado actual: "
                    + estado + ").");
        }
        FacturaCliente factura = facturaService.buscarPorPedido(pedidoId)
                .orElseThrow(() -> new ReglaNegocioException("El pedido todavía no tiene factura."));
        Optional<PagoRespuestaDTO> vigente = pagoService.pagoVigente(factura.getId());
        if (vigente.isPresent() && vigente.get().tipoPago() != TipoPago.EFECTIVO) {
            throw new ReglaNegocioException("El pago de este pedido es por " + vigente.get().tipoPago()
                    + ": lo confirma administración.");
        }
        return pagoService.confirmarFactura(factura.getId(), TipoPago.EFECTIVO);
    }

    // ---------- Auxiliares ----------

    private Entrega buscar(String pedidoId) {
        return entregaRepository.findByPedidoId(pedidoId)
                .filter(e -> !e.isEliminado())
                .orElseThrow(() -> new RecursoNoEncontradoException("Entrega del pedido", pedidoId));
    }

    /** Ve: administración o el transportista asignado. */
    private void exigirAcceso(Entrega entrega) {
        UsuarioAutenticado usuario = usuarioActual.obtener();
        if (usuario.getRol().esAdministracion()) {
            return;
        }
        exigirPropietario(entrega);
    }

    /** Opera: únicamente el transportista asignado a esa entrega. */
    private void exigirPropietario(Entrega entrega) {
        UsuarioAutenticado usuario = usuarioActual.obtener();
        Transportista asignado = entrega.getTransportista();
        if (usuario.getRol() != Rol.TRANSPORTISTA || asignado == null
                || !asignado.getId().equals(usuario.getEmpleadoId())) {
            throw new AccessDeniedException("La entrega pertenece a otro transportista");
        }
    }

    private EntregaResumenDTO aResumen(Entrega entrega) {
        Pedido pedido = entrega.getPedido();
        Optional<PagoRespuestaDTO> pago = facturaService.buscarPorPedido(pedido.getId())
                .flatMap(f -> pagoService.pagoVigente(f.getId()));
        return new EntregaResumenDTO(
                pedido.getId(),
                pedido.getCliente().getNombreComercio(),
                texto(pedido.getCliente().getDireccion()),
                entrega.getFechaDespacho(),
                entrega.getFechaEntrega(),
                pedido.getTotal(),
                pago.map(PagoRespuestaDTO::tipoPago).orElse(null),
                pago.map(PagoRespuestaDTO::estado).orElse(null));
    }

    private static String texto(Direccion d) {
        return d.getCalle() + " " + d.getNumeracion() + ", " + d.getLocalidadNombre();
    }
}