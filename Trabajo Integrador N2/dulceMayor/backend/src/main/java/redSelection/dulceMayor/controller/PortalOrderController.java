package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import redSelection.dulceMayor.dto.CambioEstadoPedidoDTO;
import redSelection.dulceMayor.dto.PedidoCrearDTO;
import redSelection.dulceMayor.dto.PedidoRespuestaDTO;
import redSelection.dulceMayor.service.PedidoService;

/**
 * Pedidos. PATRÓN: Template Method (capa Controller) + DTO.
 * Alta, listado y detalle los hereda de BaseController; toda la lógica (quién ve qué, promociones, facturación)
 * está en PedidoService. Modificar o borrar un pedido no se permite: se anula con un cambio de estado.
 */
@RestController
@RequestMapping("/api/orders")
public class PortalOrderController extends BaseController<PedidoCrearDTO, PedidoRespuestaDTO> {

    private final PedidoService pedidos;

    public PortalOrderController(PedidoService pedidos) {
        super(pedidos);
        this.pedidos = pedidos;
    }

    @Override protected String idDe(PedidoRespuestaDTO respuesta) { return respuesta.id(); }

    /** Administración: facturar, entregar o anular (lo valida el patrón State del pedido). */
    @PatchMapping("/{id}/status")
    public PedidoRespuestaDTO cambiarEstado(@PathVariable String id, @Valid @RequestBody CambioEstadoPedidoDTO cambio) {
        return pedidos.cambiarEstado(id, cambio);
    }
}
