package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.NotBlank;

public record CambioClaveDTO(
        @NotBlank(message = "Ingresá la clave nueva")
        String newPassword
) { }
