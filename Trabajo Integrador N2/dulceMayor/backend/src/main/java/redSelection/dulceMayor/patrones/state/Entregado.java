package redSelection.dulceMayor.patrones.state;

import redSelection.dulceMayor.enums.EstadoPedido;

/** Estado final: el comercio recibió el pedido. No admite ninguna transición. */
final class Entregado implements EstadoPedidoState {

    @Override
    public EstadoPedido nombre() {
        return EstadoPedido.ENTREGADO;
    }
}
