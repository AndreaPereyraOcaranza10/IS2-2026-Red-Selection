package redSelection.dulceMayor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/* El responsable se indica con responsableId (existente) O responsableNuevo; el service exige exactamente uno. */
public record PuntoVentaCrearDTO(
        @NotBlank @Size(max = 100) String nombreComercio,
        @Size(max = 150) String razonSocial,   // opcional: por defecto = nombreComercio
        @NotBlank @Pattern(regexp = PatronesValidacion.CUIT, message = "El CUIT tiene que tener 11 dígitos") String cuit,
        @NotNull @Valid DireccionCrearDTO direccion,
        String responsableId,
        @Valid ResponsableCrearDTO responsableNuevo
) { }