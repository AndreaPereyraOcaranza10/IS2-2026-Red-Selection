package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.DespachoCrearDTO;
import redSelection.dulceMayor.dto.DespachoRespuestaDTO;
import redSelection.dulceMayor.dto.TransportistaDespachoDTO;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Pedido;
import redSelection.dulceMayor.model.Transportista;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Despacho: administración asigna pedidos FACTURADOS a un transportista.
 * No extiende BaseService a propósito: no es un CRUD, es un caso de uso que coordina a otros services, sin tocar
 * ningún repository: TransportistaService (quién), PedidoService (FACTURADO -> EN_REPARTO con el State) y
 * RutaService (la Entrega y las paradas de su ruta). Todo en una transacción: se despachan todos los pedidos o ninguno.
 */
@Service
public class DespachoService {

    private final TransportistaService transportistaService;
    private final PedidoService pedidoService;
    private final RutaService rutaService;

    public DespachoService(TransportistaService transportistaService, PedidoService pedidoService,
                           RutaService rutaService) {
        this.transportistaService = transportistaService;
        this.pedidoService = pedidoService;
        this.rutaService = rutaService;
    }

    /** Transportistas activos para el selector, ordenados por apellido. */
    @Transactional(readOnly = true)
    public List<TransportistaDespachoDTO> transportistasDisponibles() {
        return transportistaService.activos().stream()
                .sorted(Comparator.comparing(Transportista::getApellido, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Transportista::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(t -> new TransportistaDespachoDTO(t.getId(), nombreCompleto(t), t.getPatenteVehiculo()))
                .toList();
    }

    @Transactional
    public DespachoRespuestaDTO asignar(DespachoCrearDTO dto) {
        Transportista transportista = transportistaService.obtenerEntidad(dto.transportistaId());
        if (!transportistaService.estaActivo(transportista)) {
            throw new ReglaNegocioException(nombreCompleto(transportista) + " está dado de baja: elegí otro transportista.");
        }
        LocalDateTime ahora = LocalDateTime.now();
        List<Pedido> pedidos = pedidoService.despacharFacturados(dto.pedidoIds(), "Asignado a " + nombreCompleto(transportista));
        rutaService.incorporarPedidosARuta(transportista, pedidos, ahora);

        return new DespachoRespuestaDTO(transportista.getId(), nombreCompleto(transportista),
                pedidos.stream().map(Pedido::getId).toList(), ahora);
    }

    private String nombreCompleto(Transportista t) {
        return t.getNombre() + " " + t.getApellido();
    }
}