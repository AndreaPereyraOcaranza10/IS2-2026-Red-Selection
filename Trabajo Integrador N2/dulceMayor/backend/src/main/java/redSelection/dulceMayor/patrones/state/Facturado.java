package redSelection.dulceMayor.patrones.state;

import redSelection.dulceMayor.enums.EstadoPedido;

/** Pedido facturado: sale a reparto cuando se asigna a una ruta, o se anula antes de salir. */
final class Facturado implements EstadoPedidoState {

    @Override
    public EstadoPedido nombre() {
        return EstadoPedido.FACTURADO;
    }

    @Override
    public EstadoPedido despachar() {
        return EstadoPedido.EN_REPARTO;
    }

    @Override
    public EstadoPedido anular() {
        return EstadoPedido.ANULADO;
    }
}
