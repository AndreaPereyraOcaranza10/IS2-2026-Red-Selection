package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.enums.EstadoPago;
import redSelection.dulceMayor.enums.TipoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Una fila del historial de entregas del transportista. */
public record EntregaResumenDTO(
        String pedidoId,
        String comercio,
        String direccion,
        LocalDateTime fechaDespacho,
        LocalDateTime fechaEntrega,
        BigDecimal total,
        TipoPago medioPago,      // null si todavía no se acordó ninguno
        EstadoPago estadoPago    // null si no hay pago registrado
) { }