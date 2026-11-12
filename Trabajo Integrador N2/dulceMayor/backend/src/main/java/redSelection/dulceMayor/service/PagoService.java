package redSelection.dulceMayor.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.PagoCrearDTO;
import redSelection.dulceMayor.dto.PagoRespuestaDTO;
import redSelection.dulceMayor.enums.EstadoFactura;
import redSelection.dulceMayor.enums.EstadoPago;
import redSelection.dulceMayor.enums.EstadoPedido;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.FacturaCliente;
import redSelection.dulceMayor.model.Pago;
import redSelection.dulceMayor.model.Pedido;
import redSelection.dulceMayor.patrones.factory.MedioPago;
import redSelection.dulceMayor.patrones.factory.MedioPagoFactory;
import redSelection.dulceMayor.repository.PagoRepository;
import redSelection.dulceMayor.security.UsuarioActual;
import redSelection.dulceMayor.security.UsuarioAutenticado;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Ciclo del pago de un PEDIDO: iniciar (se elige el medio) → confirmar o anular. La factura se emite recién al
 * confirmar el pago (o al despachar, si es efectivo): el pago cuelga del pedido y se vincula a la factura cuando existe.
 *
 * No extiende BaseService a propósito: no es un CRUD sino un proceso con reglas de estado (Template Method
 * aplica a los ABM). Las diferencias entre medios las resuelven MedioPagoFactory / MedioPago. PagoService NO conoce
 * a PedidoService (PedidoService es quien lo usa): recibe el Pedido de quien lo invoca.
 */
@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final FacturaClienteService facturaService;
    private final MedioPagoFactory medioPagoFactory;
    private final UsuarioActual usuarioActual;

    public PagoService(PagoRepository pagoRepository,
                       FacturaClienteService facturaService,
                       MedioPagoFactory medioPagoFactory,
                       UsuarioActual usuarioActual) {
        this.pagoRepository = pagoRepository;
        this.facturaService = facturaService;
        this.medioPagoFactory = medioPagoFactory;
        this.usuarioActual = usuarioActual;
    }

    // =====================================================
    // POR PEDIDO (lo usa PedidoService)
    // =====================================================

    /**
     * Elige el medio de pago de un pedido que espera el pago. Si ya había un pago pendiente de otro medio, se anula:
     * queda uno solo vigente. El monto sale siempre del pedido.
     */
    @Transactional
    public PagoRespuestaDTO iniciar(Pedido pedido, TipoPago tipoPago) {
        MedioPago medio = medioPagoFactory.obtener(tipoPago);
        if (!medio.disponible()) {
            throw new ReglaNegocioException("El medio de pago " + tipoPago + " todavía no está habilitado.");
        }
        if (pedido.getEstado() != EstadoPedido.PENDIENTE_PAGO) {
            throw new ReglaNegocioException("Solo se puede pagar un pedido que está esperando el pago (estado actual: "
                    + pedido.getEstado() + ").");
        }
        List<Pago> existentes = pagoRepository.findByPedidoIdAndEliminadoFalse(pedido.getId());
        if (existentes.stream().anyMatch(p -> p.getEstado() == EstadoPago.CONFIRMADO)) {
            throw new ReglaNegocioException("El pedido ya está pago.");
        }
        existentes.stream().filter(p -> p.getEstado() == EstadoPago.PENDIENTE)
                .forEach(p -> p.setEstado(EstadoPago.ANULADO));

        Pago pago = pagoRepository.save(medio.crearPago(pedido));
        medio.alGuardar(pago);
        return aRespuesta(pago);
    }

    /**
     * Registra el cobro de un pedido con un medio (idempotente): si ya hay un pago confirmado del mismo medio lo
     * devuelve; si había uno pendiente de ese medio lo confirma; si no, lo crea y lo confirma. Al confirmar se
     * emite la factura.
     */
    @Transactional
    public PagoRespuestaDTO confirmarPedido(Pedido pedido, TipoPago tipoPago) {
        List<Pago> existentes = pagoRepository.findByPedidoIdAndEliminadoFalse(pedido.getId());
        Pago confirmado = existentes.stream().filter(p -> p.getEstado() == EstadoPago.CONFIRMADO).findFirst().orElse(null);
        if (confirmado != null) {
            if (confirmado.getTipoPago() == tipoPago) {
                return aRespuesta(confirmado);
            }
            throw new ReglaNegocioException("El pedido ya fue pagado con " + confirmado.getTipoPago() + ".");
        }
        String pagoId = existentes.stream()
                .filter(p -> p.getEstado() == EstadoPago.PENDIENTE && p.getTipoPago() == tipoPago)
                .map(Pago::getId).findFirst()
                .orElseGet(() -> iniciar(pedido, tipoPago).id());
        return confirmar(pagoId);
    }

    /** El pago que hoy vale para un pedido (el confirmado, si no el pendiente). Sin control de acceso: lo valida quien llama. */
    @Transactional(readOnly = true)
    public Optional<PagoRespuestaDTO> pagoVigentePorPedido(String pedidoId) {
        List<Pago> pagos = pagoRepository.findByPedidoIdAndEliminadoFalse(pedidoId);
        return pagos.stream().filter(p -> p.getEstado() == EstadoPago.CONFIRMADO).findFirst()
                .or(() -> pagos.stream().filter(p -> p.getEstado() == EstadoPago.PENDIENTE).findFirst())
                .map(this::aRespuesta);
    }

    /** Al anular o vencer un pedido: sus pagos pendientes se anulan (el confirmado no se toca). */
    @Transactional
    public void anularPendientesDe(String pedidoId) {
        pagoRepository.findByPedidoIdAndEliminadoFalse(pedidoId).stream()
                .filter(p -> p.getEstado() == EstadoPago.PENDIENTE)
                .forEach(p -> p.setEstado(EstadoPago.ANULADO));
    }

    /** Al emitir la factura de un pedido sin pagar (efectivo): el pago vigente pasa a apuntar a esa factura. */
    @Transactional
    public void vincularFactura(String pedidoId, FacturaCliente factura) {
        pagoRepository.findByPedidoIdAndEliminadoFalse(pedidoId).stream()
                .filter(p -> p.getEstado() != EstadoPago.ANULADO && p.getFactura() == null)
                .forEach(p -> p.setFactura(factura));
    }

    // =====================================================
    // POR FACTURA (contrato de BK-07: lo usa EntregaService para el cobro en efectivo)
    // =====================================================

    @Transactional
    public PagoRespuestaDTO iniciar(String facturaId, PagoCrearDTO dto) {
        MedioPago medio = medioPagoFactory.obtener(dto.tipoPago());
        if (!medio.disponible()) {
            throw new ReglaNegocioException("El medio de pago " + dto.tipoPago() + " todavía no está habilitado.");
        }
        FacturaCliente factura = facturaService.obtenerEntidad(facturaId);
        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new ReglaNegocioException("La factura está anulada: no se puede pagar.");
        }
        List<Pago> existentes = pagosDe(factura);
        if (factura.getEstado() == EstadoFactura.PAGADA
                || existentes.stream().anyMatch(p -> p.getEstado() == EstadoPago.CONFIRMADO)) {
            throw new ReglaNegocioException("La factura ya está paga.");
        }
        existentes.stream().filter(p -> p.getEstado() == EstadoPago.PENDIENTE)
                .forEach(p -> p.setEstado(EstadoPago.ANULADO));

        Pago pago = medio.crearPago(factura.getPedido());
        pago.setFactura(factura);
        pago = pagoRepository.save(pago);
        medio.alGuardar(pago);
        return aRespuesta(pago);
    }

    @Transactional(readOnly = true)
    public List<PagoRespuestaDTO> listarPorFactura(String facturaId) {
        FacturaCliente factura = facturaService.obtenerEntidad(facturaId);
        exigirAcceso(factura.getPedido());
        return pagosDe(factura).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public PagoRespuestaDTO obtener(String pagoId) {
        Pago pago = buscar(pagoId);
        exigirAcceso(pedidoDe(pago));
        return aRespuesta(pago);
    }

    /**
     * El pago que hoy vale para una factura: el confirmado si lo hay, si no el pendiente. SIN control de acceso:
     * lo usan otros services que ya validaron quién pregunta (ej. EntregaService para el transportista).
     */
    @Transactional(readOnly = true)
    public Optional<PagoRespuestaDTO> pagoVigente(String facturaId) {
        List<Pago> pagosFactura = pagoRepository.findByFacturaIdAndEliminadoFalse(facturaId);
        return pagosFactura.stream().filter(p -> p.getEstado() == EstadoPago.CONFIRMADO).findFirst()
                .or(() -> pagosFactura.stream().filter(p -> p.getEstado() == EstadoPago.PENDIENTE).findFirst())
                .map(this::aRespuesta);
    }

    /**
     * Contrato de BK-07: registra el cobro de una factura con un medio. Es IDEMPOTENTE: repetir la misma
     * llamada no crea otro pago ni cambia nada, devuelve el pago ya confirmado. Si había un pago pendiente
     * de ese medio lo confirma; si no, lo crea y lo confirma (efectivo o transferencia recibidos).
     */
    @Transactional
    public PagoRespuestaDTO confirmarFactura(String facturaId, TipoPago tipoPago) {
        FacturaCliente factura = facturaService.obtenerEntidad(facturaId);   // 404 si no existe
        List<Pago> existentes = pagosDe(factura);
        Pago confirmado = existentes.stream().filter(p -> p.getEstado() == EstadoPago.CONFIRMADO).findFirst().orElse(null);
        if (confirmado != null) {
            if (confirmado.getTipoPago() == tipoPago) {
                return aRespuesta(confirmado);
            }
            throw new ReglaNegocioException("La factura ya fue pagada con " + confirmado.getTipoPago() + ".");
        }
        String pagoId = existentes.stream()
                .filter(p -> p.getEstado() == EstadoPago.PENDIENTE && p.getTipoPago() == tipoPago)
                .map(Pago::getId).findFirst()
                .orElseGet(() -> iniciar(facturaId, new PagoCrearDTO(tipoPago)).id());
        return confirmar(pagoId);
    }

    // =====================================================
    // CONFIRMAR / ANULAR
    // =====================================================

    /**
     * Idempotente: confirmar un pago que ya está confirmado devuelve lo mismo y no cambia nada (ni la fecha).
     * Bloqueo pesimista: dos confirmaciones simultáneas no pueden registrar el mismo cobro dos veces.
     * Si el pedido todavía no tiene factura (transferencia, Mercado Pago, efectivo cobrado antes de despachar),
     * se emite acá, en la misma transacción: o queda pago y facturado, o no queda nada.
     */
    @Transactional
    public PagoRespuestaDTO confirmar(String pagoId) {
        Pago pago = pagoRepository.buscarConBloqueo(pagoId)
                .filter(p -> !p.isEliminado())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", pagoId));
        Rol rol = usuarioActual.obtener().getRol();
        if (!medioPagoFactory.obtener(pago.getTipoPago()).puedeConfirmar(rol)) {
            throw new AccessDeniedException("Tu rol no puede confirmar pagos de este medio");
        }
        if (pago.getEstado() == EstadoPago.CONFIRMADO) {
            return aRespuesta(pago);
        }
        exigirPendiente(pago, "confirmar");
        return aRespuesta(acreditar(pago));
    }

    /**
     * Acredita el pago: lo marca CONFIRMADO, emite la factura si el pedido todavía no la tiene y la deja PAGADA.
     * Lo comparten confirmar (empleados) y la confirmación por la pasarela de pago (sin usuario logueado).
     */
    private Pago acreditar(Pago pago) {
        FacturaCliente factura = pago.getFactura();
        if (factura == null) {
            factura = facturaService.facturarPedido(pedidoDe(pago), usuarioActual.nombreOSistema());
            pago.setFactura(factura);
        }
        pago.setEstado(EstadoPago.CONFIRMADO);
        pago.setFechaPago(LocalDateTime.now());
        facturaService.marcarPagada(factura);   // criterio de BK-07: la factura pasa a pagada
        return pago;
    }

    @Transactional
    public PagoRespuestaDTO anular(String pagoId) {
        Pago pago = pagoRepository.buscarConBloqueo(pagoId)
                .filter(p -> !p.isEliminado())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", pagoId));
        exigirPendiente(pago, "anular");
        pago.setEstado(EstadoPago.ANULADO);
        return aRespuesta(pago);
    }

    // ---------- Auxiliares ----------

    private Pago buscar(String id) {
        return pagoRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
    }

    /** Pagos de una factura: los que apuntan a ella y, si todavía no se vincularon, los de su pedido. */
    private List<Pago> pagosDe(FacturaCliente factura) {
        List<Pago> pagos = new java.util.ArrayList<>(pagoRepository.findByFacturaIdAndEliminadoFalse(factura.getId()));
        pagoRepository.findByPedidoIdAndEliminadoFalse(factura.getPedido().getId()).stream()
                .filter(p -> pagos.stream().noneMatch(x -> x.getId().equals(p.getId())))
                .forEach(pagos::add);
        return pagos;
    }

    /** Pedido del pago (los pagos viejos, anteriores a este cambio, solo conocen su factura). */
    private static Pedido pedidoDe(Pago pago) {
        return pago.getPedido() != null ? pago.getPedido() : pago.getFactura().getPedido();
    }

    private static void exigirPendiente(Pago pago, String accion) {
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new ReglaNegocioException("Solo se puede " + accion + " un pago pendiente (estado actual: "
                    + pago.getEstado() + ").");
        }
    }

    /** La administración ve todo; el cliente, solo los pagos de su comercio. */
    private void exigirAcceso(Pedido pedido) {
        UsuarioAutenticado usuario = usuarioActual.obtener();
        if (usuario.getRol().esAdministracion()) {
            return;
        }
        String responsableId = pedido.getCliente().getResponsable().getId();
        if (usuario.getRol() != Rol.CLIENTE || !responsableId.equals(usuario.getEmpleadoId())) {
            throw new AccessDeniedException("El pago pertenece a otro cliente");
        }
    }

    private PagoRespuestaDTO aRespuesta(Pago pago) {
        FacturaCliente factura = pago.getFactura();
        return new PagoRespuestaDTO(
                pago.getId(),
                pedidoDe(pago).getId(),
                factura == null ? null : factura.getId(),
                factura == null ? null : factura.getNumeroFactura(),
                pago.getTipoPago(),
                pago.getEstado(),
                pago.getMonto(),
                pago.getFechaPago(),
                medioPagoFactory.obtener(pago.getTipoPago()).instrucciones(pago),
                pago.getCodigoQr());
    }
}