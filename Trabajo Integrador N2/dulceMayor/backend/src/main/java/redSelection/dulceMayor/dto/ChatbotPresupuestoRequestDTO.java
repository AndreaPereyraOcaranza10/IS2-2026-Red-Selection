package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatbotPresupuestoRequestDTO(
        @NotBlank(message = "Escribí qué productos necesitás")
        @Size(max = 1000, message = "La consulta no puede superar los 1000 caracteres")
        String mensaje
) { }
