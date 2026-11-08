package redSelection.dulceMayor.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.PagoCrearDTO;
import redSelection.dulceMayor.dto.PagoRespuestaDTO;
import redSelection.dulceMayor.enums.EstadoFactura;
import redSelection.dulceMayor.enums.EstadoPago;
import redSelection.dulceMayor.enums.Rol;
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
        FacturaCliente factura = facturaService.obtenerEntidad(facturaId);
        if (factura.getEstado() != EstadoFactura.EMITIDA) {
            throw new ReglaNegocioException("Solo se puede pagar una factura emitida.");
        }
        List<Pago> existentes = pagoRepository.findByFacturaIdAndEliminadoFalse(facturaId);
        if (existentes.stream().anyMatch(p -> p.getEstado() == EstadoPago.CONFIRMADO)) {
            throw new ReglaNegocioException("La factura ya está paga.");
        }
        // Si el cliente cambia de medio, el pago pendiente anterior se anula: queda uno solo vigente.
        existentes.stream().filter(p -> p.getEstado() == EstadoPago.PENDIENTE)
                .forEach(p -> p.setEstado(EstadoPago.ANULADO));

        MedioPago medio = medioPagoFactory.obtener(dto.tipoPago());
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

    /** Bloqueo pesimista: dos confirmaciones simultáneas no pueden registrar el mismo cobro dos veces. */
    @Transactional
    public PagoRespuestaDTO confirmar(String pagoId) {
        Pago pago = pagoRepository.buscarConBloqueo(pagoId)
                .filter(p -> !p.isEliminado())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", pagoId));
        Rol rol = usuarioActual.obtener().getRol();
        if (!medioPagoFactory.obtener(pago.getTipoPago()).puedeConfirmar(rol)) {
            throw new AccessDeniedException("Tu rol no puede confirmar pagos de este medio");
        }
        exigirPendiente(pago, "confirmar");
        if (pago.getFactura().getEstado() != EstadoFactura.EMITIDA) {
            throw new ReglaNegocioException("La factura está anulada: no se puede registrar el pago.");
        }
        pago.setEstado(EstadoPago.CONFIRMADO);
        pago.setFechaPago(LocalDateTime.now());
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
