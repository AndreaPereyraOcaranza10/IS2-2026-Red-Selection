package redSelection.dulceMayor.patrones.state;

import redSelection.dulceMayor.enums.EstadoPedido;

/** Pedido recién cargado: si hay stock se reserva y espera el pago; si no, espera stock; o se anula. */
final class Cargado implements EstadoPedidoState {

    @Override
    public EstadoPedido nombre() {
        return EstadoPedido.CARGADO;
    }

    @Override
    public EstadoPedido esperarPago() {
        return EstadoPedido.PENDIENTE_PAGO;
    }

    @Override
    public EstadoPedido quedarPendienteDeStock() {
        return EstadoPedido.PENDIENTE_STOCK;
    }

    @Override
    public EstadoPedido anular() {
        return EstadoPedido.ANULADO;
    }
}