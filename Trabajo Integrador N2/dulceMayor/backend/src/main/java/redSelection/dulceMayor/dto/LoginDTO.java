package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank(message = "Ingresá tu usuario")
        String username,
        @NotBlank(message = "Ingresá tu clave")
        String password
) { }
