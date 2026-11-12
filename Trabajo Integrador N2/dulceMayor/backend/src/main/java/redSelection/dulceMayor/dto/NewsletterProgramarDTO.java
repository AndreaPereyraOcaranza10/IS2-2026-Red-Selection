package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Demora en minutos para programar un envío único de demostración. */
public record NewsletterProgramarDTO(
        @NotNull @Min(1) @Max(1440) Long minutos
) { }
