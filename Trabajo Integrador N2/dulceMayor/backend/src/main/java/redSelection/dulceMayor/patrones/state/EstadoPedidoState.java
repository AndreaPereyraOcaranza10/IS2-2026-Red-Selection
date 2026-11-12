package redSelection.dulceMayor.patrones.state;

import redSelection.dulceMayor.enums.EstadoPedido;
import redSelection.dulceMayor.exception.ReglaNegocioException;

/**
 * PATRÓN: State (comportamiento). Cada estado del pedido es una clase que decide qué transiciones permite.
 * Cada acción devuelve el estado al que pasa el pedido. Por defecto todas se rechazan: cada estado
 * sobrescribe solo las que admite, así una transición nueva no se puede colar sin querer.
 *
 * Transiciones permitidas:
 *   CARGADO          -> PENDIENTE_PAGO (stock reservado), PENDIENTE_STOCK, ANULADO
 *   PENDIENTE_STOCK  -> PENDIENTE_PAGO, ANULADO
 *   PENDIENTE_PAGO   -> FACTURADO (se acreditó el pago o se despacha en efectivo), ANULADO
 *   FACTURADO        -> EN_REPARTO, ANULADO
 *   EN_REPARTO       -> ENTREGADO
 *   ENTREGADO, ANULADO: finales
 */
public interface EstadoPedidoState {

    EstadoPedido nombre();

    default EstadoPedido esperarPago() {
        throw transicionInvalida(EstadoPedido.PENDIENTE_PAGO);
    }

    default EstadoPedido facturar() {
        throw transicionInvalida(EstadoPedido.FACTURADO);
    }

    default EstadoPedido quedarPendienteDeStock() {
        throw transicionInvalida(EstadoPedido.PENDIENTE_STOCK);
    }

    default EstadoPedido despachar() {
        throw transicionInvalida(EstadoPedido.EN_REPARTO);
    }

    default EstadoPedido entregar() {
        throw transicionInvalida(EstadoPedido.ENTREGADO);
    }

    default EstadoPedido anular() {
        throw transicionInvalida(EstadoPedido.ANULADO);
    }

    default ReglaNegocioException transicionInvalida(EstadoPedido destino) {
        return new ReglaNegocioException(
                "No se puede pasar un pedido de " + nombre() + " a " + destino + ".");
    }
}