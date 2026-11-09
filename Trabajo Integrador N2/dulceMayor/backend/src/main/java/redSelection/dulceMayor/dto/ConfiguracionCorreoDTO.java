package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Asunto y cuerpo HTML de un tipo de correo. El cuerpo puede llevar {{marcadores}} (ver ConfiguracionCorreoService). */
public record ConfiguracionCorreoDTO(
        @NotBlank @Size(max = 150, message = "El asunto puede tener hasta 150 caracteres") String asunto,
        @NotBlank @Size(max = 20000, message = "El cuerpo puede tener hasta 20000 caracteres") String cuerpoHtml
) { }
