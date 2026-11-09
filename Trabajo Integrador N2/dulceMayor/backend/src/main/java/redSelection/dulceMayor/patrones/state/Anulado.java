package redSelection.dulceMayor.patrones.state;

import redSelection.dulceMayor.enums.EstadoPedido;

/** Estado final: el pedido se canceló. No admite ninguna transición. */
final class Anulado implements EstadoPedidoState {

    @Override
    public EstadoPedido nombre() {
        return EstadoPedido.ANULADO;
    }
}
