package redSelection.dulceMayor.patrones.state;

import redSelection.dulceMayor.enums.EstadoPedido;

/** Pedido recién cargado: se factura si hay stock, queda esperando stock si no, o se anula. */
final class Cargado implements EstadoPedidoState {

    @Override
    public EstadoPedido nombre() {
        return EstadoPedido.CARGADO;
    }

    @Override
    public EstadoPedido facturar() {
        return EstadoPedido.FACTURADO;
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
