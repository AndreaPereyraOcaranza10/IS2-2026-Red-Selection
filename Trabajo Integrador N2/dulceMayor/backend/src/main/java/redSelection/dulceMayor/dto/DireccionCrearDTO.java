package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.*;

public record DireccionCrearDTO(
        @NotBlank @Size(max = 100)
        @Pattern(regexp = PatronesValidacion.CALLE, message = "La calle tiene caracteres no válidos")
        String calle,

        @NotBlank
        @Pattern(regexp = PatronesValidacion.NUMERACION, message = "La numeración solo lleva números")
        String numeracion,

        @Size(max = 100) String barrio,
        @Size(max = 200) String referencia,

        @NotBlank @Pattern(regexp = PatronesValidacion.ID_GEOREF, message = "Provincia no válida") String provinciaId,
        @NotBlank @Pattern(regexp = PatronesValidacion.ID_GEOREF, message = "Departamento no válido") String departamentoId,
        @NotBlank @Pattern(regexp = PatronesValidacion.ID_GEOREF, message = "Localidad no válida") String localidadId,

        @DecimalMin(value = "-56.0", message = "La latitud está fuera de Argentina")
        @DecimalMax(value = "-21.0", message = "La latitud está fuera de Argentina")
        Double latitud,

        @DecimalMin(value = "-74.0", message = "La longitud está fuera de Argentina")
        @DecimalMax(value = "-53.0", message = "La longitud está fuera de Argentina")
        Double longitud
) { }
