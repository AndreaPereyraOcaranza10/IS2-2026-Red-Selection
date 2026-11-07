package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.NotNull;
import redSelection.dulceMayor.enums.TipoMovimientoStock;

public record MovimientoStockDTO(

        int movimiento,

        @NotNull
        TipoMovimientoStock tipoMovimiento,

        String observacion
) {}
