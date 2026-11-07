package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ActualizarPrecioDTO(

        @NotNull
        @Positive
        BigDecimal precio

) {}

