package redSelection.dulceMayor.dto;

import java.math.BigDecimal;

/**
 * Resultado de validar un código. Si es válido trae las reglas para que el carrito muestre el descuento:
 * type VOLUMEN -> aplica sobre todo el pedido si se llega a minQuantity unidades;
 * type PRODUCTO_DESTACADO -> aplica solo sobre las líneas de productId.
 * El descuento definitivo lo calcula el backend al cargar el pedido.
 */
public record PromocionValidadaDTO(boolean valid, BigDecimal discountPercent, String type,
                                   Integer minQuantity, String productId, String productName,
                                   String message) {

    public static PromocionValidadaDTO invalida(String mensaje) {
        return new PromocionValidadaDTO(false, BigDecimal.ZERO, null, null, null, null, mensaje);
    }
}
