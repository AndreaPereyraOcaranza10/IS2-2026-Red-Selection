package redSelection.dulceMayor.patrones.state;

import redSelection.dulceMayor.enums.EstadoPedido;

/**
 * Pedido con el stock reservado que espera el pago del cliente. Se factura cuando el pago se acredita
 * (transferencia o Mercado Pago) o cuando se despacha, si se cobra en efectivo al entregar. Se puede anular
 * (por el cliente, por administración o por vencimiento) y la reserva se libera sola.
 */
final class PendientePago implements EstadoPedidoState {

    @Override
    public EstadoPedido nombre() {
        return EstadoPedido.PENDIENTE_PAGO;
    }

    @Override
    public EstadoPedido facturar() {
        return EstadoPedido.FACTURADO;
    }

    @Override
    public EstadoPedido anular() {
        return EstadoPedido.ANULADO;
    }
}