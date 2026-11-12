package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redSelection.dulceMayor.dto.CambioEstadoPedidoDTO;
import redSelection.dulceMayor.dto.PagoCrearDTO;
import redSelection.dulceMayor.dto.PagoRespuestaDTO;
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

    /** Administración: reintentar la reserva de stock, entregar o anular (lo valida el patrón State del pedido). */
    @PatchMapping("/{id}/status")
    public PedidoRespuestaDTO cambiarEstado(@PathVariable String id, @Valid @RequestBody CambioEstadoPedidoDTO cambio) {
        return pedidos.cambiarEstado(id, cambio);
    }

    /** El cliente (o administración) cancela un pedido que todavía no se facturó: se libera el stock reservado. */
    @PostMapping("/{id}/cancel")
    public PedidoRespuestaDTO cancelar(@PathVariable String id) {
        return pedidos.cancelar(id);
    }

    /** Elige el medio de pago del pedido (el cliente, billetera virtual; administración, cualquiera). */
    @PostMapping("/{id}/pagos")
    public ResponseEntity<PagoRespuestaDTO> iniciarPago(@PathVariable String id, @Valid @RequestBody PagoCrearDTO dto) {
        return ResponseEntity.status(201).body(pedidos.iniciarPago(id, dto));
    }

    /** Administración registra el cobro (transferencia acreditada, efectivo recibido): se emite la factura. */
    @PostMapping("/{id}/pagos/confirmar")
    public PagoRespuestaDTO registrarPago(@PathVariable String id, @Valid @RequestBody PagoCrearDTO dto) {
        return pedidos.registrarPago(id, dto);
    }

    /** El pago vigente del pedido (instrucciones y QR); 204 si todavía no se eligió un medio. */
    @GetMapping("/{id}/pagos/vigente")
    public ResponseEntity<PagoRespuestaDTO> pagoVigente(@PathVariable String id) {
        return pedidos.pagoVigente(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}