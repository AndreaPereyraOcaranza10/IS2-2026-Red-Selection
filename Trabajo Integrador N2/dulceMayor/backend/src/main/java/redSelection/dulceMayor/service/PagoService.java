package redSelection.dulceMayor.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.PagoCrearDTO;
import redSelection.dulceMayor.dto.PagoRespuestaDTO;
import redSelection.dulceMayor.enums.EstadoFactura;
import redSelection.dulceMayor.enums.EstadoPago;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.FacturaCliente;
import redSelection.dulceMayor.model.Pago;
import redSelection.dulceMayor.patrones.factory.MedioPago;
import redSelection.dulceMayor.patrones.factory.MedioPagoFactory;
import redSelection.dulceMayor.repository.PagoRepository;
import redSelection.dulceMayor.security.UsuarioActual;
import redSelection.dulceMayor.security.UsuarioAutenticado;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Ciclo del pago de una factura: iniciar (la administración acuerda el medio con el cliente) → confirmar o anular.
 * No extiende BaseService a propósito: no es un CRUD sino un proceso con reglas de estado (Template Method
 * aplica a los ABM). Las diferencias entre medios las resuelven MedioPagoFactory / MedioPago.
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
        List<Pago> existentes = pagoRepository.findByFacturaIdAndEliminadoFalse(facturaId);
        if (factura.getEstado() == EstadoFactura.PAGADA
                || existentes.stream().anyMatch(p -> p.getEstado() == EstadoPago.CONFIRMADO)) {
            throw new ReglaNegocioException("La factura ya está paga.");
        }
        // Si el cliente cambia de medio, el pago pendiente anterior se anula: queda uno solo vigente.
        existentes.stream().filter(p -> p.getEstado() == EstadoPago.PENDIENTE)
                .forEach(p -> p.setEstado(EstadoPago.ANULADO));

        Pago pago = pagoRepository.save(medio.crearPago(factura));
        medio.alGuardar(pago);
        return aRespuesta(pago);
    }

    @Transactional(readOnly = true)
    public List<PagoRespuestaDTO> listarPorFactura(String facturaId) {
        FacturaCliente factura = facturaService.obtenerEntidad(facturaId);
        exigirAcceso(factura);
        return pagoRepository.findByFacturaIdAndEliminadoFalse(facturaId).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public PagoRespuestaDTO obtener(String pagoId) {
        Pago pago = buscar(pagoId);
        exigirAcceso(pago.getFactura());
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
        facturaService.obtenerEntidad(facturaId);   // 404 si no existe
        List<Pago> existentes = pagoRepository.findByFacturaIdAndEliminadoFalse(facturaId);
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

    /**
     * Idempotente: confirmar un pago que ya está confirmado devuelve lo mismo y no cambia nada (ni la fecha).
     * Bloqueo pesimista: dos confirmaciones simultáneas no pueden registrar el mismo cobro dos veces.
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
        if (pago.getFactura().getEstado() == EstadoFactura.ANULADA) {
            throw new ReglaNegocioException("La factura está anulada: no se puede registrar el pago.");
        }
        pago.setEstado(EstadoPago.CONFIRMADO);
        pago.setFechaPago(LocalDateTime.now());
        pago.getFactura().setEstado(EstadoFactura.PAGADA);   // criterio de BK-07: la factura pasa a pagada
        return aRespuesta(pago);
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

    private static void exigirPendiente(Pago pago, String accion) {
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new ReglaNegocioException("Solo se puede " + accion + " un pago pendiente (estado actual: "
                    + pago.getEstado() + ").");
        }
    }

    /** La administración ve todo; el cliente, solo las facturas de su comercio. */
    private void exigirAcceso(FacturaCliente factura) {
        UsuarioAutenticado usuario = usuarioActual.obtener();
        if (usuario.getRol().esAdministracion()) {
            return;
        }
        String responsableId = factura.getPedido().getCliente().getResponsable().getId();
        if (usuario.getRol() != Rol.CLIENTE || !responsableId.equals(usuario.getEmpleadoId())) {
            throw new AccessDeniedException("La factura pertenece a otro cliente");
        }
    }

    private PagoRespuestaDTO aRespuesta(Pago pago) {
        return new PagoRespuestaDTO(
                pago.getId(),
                pago.getFactura().getId(),
                pago.getFactura().getNumeroFactura(),
                pago.getTipoPago(),
                pago.getEstado(),
                pago.getMonto(),
                pago.getFechaPago(),
                medioPagoFactory.obtener(pago.getTipoPago()).instrucciones(pago),
                pago.getCodigoQr());
    }
}
