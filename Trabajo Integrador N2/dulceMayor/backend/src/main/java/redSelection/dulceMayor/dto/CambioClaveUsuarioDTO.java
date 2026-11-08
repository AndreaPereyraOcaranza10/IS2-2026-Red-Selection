package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.NotBlank;

public record CambioClaveUsuarioDTO(
        @NotBlank(message = "Ingresa la clave actual") String currentPassword,
        @NotBlank(message = "Ingresa la clave nueva") String newPassword
) { }
