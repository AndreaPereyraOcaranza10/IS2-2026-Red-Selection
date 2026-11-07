package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.*;
import redSelection.dulceMayor.enums.Presentacion;

public record ProductoActualizarDTO(

        @NotBlank
        String nombre,

        String descripcion,

        String marca,

        @NotNull
        Presentacion presentacion,

        @Min(1)
        int unidadesPorPresentacion,

        @Min(0)
        int stockMinimo,

        boolean enOferta,

        @NotBlank
        String subCategoriaId,

        String imagenId
) {}
