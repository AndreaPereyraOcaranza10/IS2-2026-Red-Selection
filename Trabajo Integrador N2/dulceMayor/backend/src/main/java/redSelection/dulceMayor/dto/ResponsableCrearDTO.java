package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.*;
import redSelection.dulceMayor.enums.TipoDocumento;
import redSelection.dulceMayor.enums.TipoTelefono;

public record ResponsableCrearDTO(
        @NotBlank @Size(max = 60)
        @Pattern(regexp = PatronesValidacion.NOMBRE_PERSONA, message = "El nombre solo puede tener letras")
        String nombre,

        @NotBlank @Size(max = 60)
        @Pattern(regexp = PatronesValidacion.NOMBRE_PERSONA, message = "El apellido solo puede tener letras")
        String apellido,

        @NotNull TipoDocumento tipoDocumento,

        @NotBlank
        @Pattern(regexp = PatronesValidacion.DOCUMENTO, message = "El documento debe tener entre 6 y 9 letras o números")
        String numeroDocumento,

        @NotBlank @Size(max = 120)
        @Email(regexp = ".+@.+\\..+", message = "Revisá el correo: falta el @ o el dominio")
        String email,

        @NotBlank
        @Pattern(regexp = PatronesValidacion.TELEFONO, message = "El teléfono solo lleva números (entre 8 y 15)")
        String telefono,

        TipoTelefono tipoTelefono   // opcional, por defecto CELULAR
) { }