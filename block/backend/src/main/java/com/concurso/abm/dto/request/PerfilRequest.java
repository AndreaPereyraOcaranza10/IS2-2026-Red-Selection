package com.concurso.abm.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PerfilRequest(
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(max = 150) String nombre,
    @NotBlank @Size(max = 150) String apellido,
    @NotBlank
        @Pattern(regexp = "[0-9]{7,8}", message = "El DNI debe tener 7 u 8 dígitos, sin puntos")
        String dni,
    @NotNull @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
        LocalDate fechaNacimiento,
    @NotNull @Valid DireccionRequest direccion) {}
