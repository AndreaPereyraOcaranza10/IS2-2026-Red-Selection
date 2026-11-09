package redSelection.dulceMayor.dto;

import java.math.BigDecimal;

/** Un renglón del pedido que se entrega. */
public record ItemEntregaDTO(
        String productoId,
        String nombre,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) { }