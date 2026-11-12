package redSelection.dulceMayor.patrones.state;

import redSelection.dulceMayor.enums.EstadoPedido;

/** Pedido sin stock suficiente: cuando llega la mercadería se reserva y espera el pago, o se anula. */
final class PendienteStock implements EstadoPedidoState {

    @Override
    public EstadoPedido nombre() {
        return EstadoPedido.PENDIENTE_STOCK;
    }

    @Override
    public EstadoPedido esperarPago() {
        return EstadoPedido.PENDIENTE_PAGO;
    }

    @Override
    public EstadoPedido anular() {
        return EstadoPedido.ANULADO;
    }
}