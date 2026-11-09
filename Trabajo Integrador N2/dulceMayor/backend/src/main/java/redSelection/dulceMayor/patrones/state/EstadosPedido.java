package redSelection.dulceMayor.patrones.state;

import redSelection.dulceMayor.enums.EstadoPedido;

import java.util.EnumMap;
import java.util.Map;

/**
 * Devuelve el objeto State de cada valor del enum. La base guarda el enum (una columna simple) y
 * Pedido lo convierte en su State al momento de cambiar de estado. Los estados no guardan datos,
 * así que hay una sola instancia de cada uno.
 */
public final class EstadosPedido {

    private static final Map<EstadoPedido, EstadoPedidoState> ESTADOS = new EnumMap<>(EstadoPedido.class);

    static {
        registrar(new Cargado());
        registrar(new PendienteStock());
        registrar(new Facturado());
        registrar(new EnReparto());
        registrar(new Entregado());
        registrar(new Anulado());
        for (EstadoPedido estado : EstadoPedido.values()) {
            if (!ESTADOS.containsKey(estado)) {
                throw new IllegalStateException("Falta la clase State de " + estado);
            }
        }
    }

    private EstadosPedido() {
    }

    private static void registrar(EstadoPedidoState estado) {
        ESTADOS.put(estado.nombre(), estado);
    }

    public static EstadoPedidoState de(EstadoPedido estado) {
        return ESTADOS.get(estado);
    }
}
