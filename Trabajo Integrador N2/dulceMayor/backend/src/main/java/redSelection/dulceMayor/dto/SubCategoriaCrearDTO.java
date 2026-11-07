package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.NotBlank;

public record SubCategoriaCrearDTO(@NotBlank String nombre, @NotBlank String categoriaId) {}
