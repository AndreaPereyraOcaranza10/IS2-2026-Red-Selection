package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.NotNull;
import redSelection.dulceMayor.enums.TipoPago;

/** El monto no viaja acá: se toma siempre del total de la factura. */
public record PagoCrearDTO(
        @NotNull(message = "Indicá el medio de pago")
        TipoPago tipoPago
) { }