package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** Ids de TODAS las paradas pendientes, en el orden en que se quieren recorrer. */
public record OrdenarRutaDTO(
        @NotEmpty
        List<String> paradaIds
) {}
