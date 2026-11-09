package redSelection.dulceMayor.patrones.state;

import redSelection.dulceMayor.enums.EstadoPedido;

/** Pedido que no se pudo facturar por falta de stock: se factura cuando llega la mercadería, o se anula. */
final class PendienteStock implements EstadoPedidoState {

    @Override
    public EstadoPedido nombre() {
        return EstadoPedido.PENDIENTE_STOCK;
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
