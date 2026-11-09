package redSelection.dulceMayor.patrones.state;

import redSelection.dulceMayor.enums.EstadoPedido;

/** Pedido en el camión: solo puede entregarse. Ya no se anula porque la mercadería salió del depósito. */
final class EnReparto implements EstadoPedidoState {

    @Override
    public EstadoPedido nombre() {
        return EstadoPedido.EN_REPARTO;
    }

    @Override
    public EstadoPedido entregar() {
        return EstadoPedido.ENTREGADO;
    }
}
