package redSelection.dulceMayor.patrones.strategy.promocion;

import redSelection.dulceMayor.enums.TipoPromocion;
import redSelection.dulceMayor.model.Promocion;

import java.math.BigDecimal;
import java.util.List;

/**
 * PATRÓN: Strategy (comportamiento). Cada tipo de promoción calcula su descuento a su manera:
 * por volumen (sobre todo el pedido si se alcanza una cantidad) o por producto destacado (solo sobre ese producto).
 * PromocionService elige la estrategia según el tipo de la promoción; agregar un tipo nuevo es sumar una clase.
 */
public interface ReglaPromocion {

    TipoPromocion tipo();

    /**
     * Descuento en pesos para esas líneas. Si la promoción no aplica (por ejemplo, no se llegó a la cantidad
     * mínima) lanza ReglaNegocioException con un mensaje para el usuario.
     */
    BigDecimal descuento(Promocion promocion, List<LineaPromocion> lineas);

    /** Una línea del pedido, tal como la necesita una regla de promoción. */
    record LineaPromocion(String productoId, int cantidad, BigDecimal subtotal) { }
}
