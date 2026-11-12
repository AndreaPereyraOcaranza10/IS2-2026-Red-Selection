package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.enums.TipoDocumento;

/** Datos para dar de alta un empleado con acceso al sistema (su usuario es su correo electrónico). */
public record UsuarioInternoCrearDTO(
        /** El correo del empleado es también su nombre de usuario para ingresar al sistema. */
        @NotBlank(message = "Ingresá el correo electrónico") @Size(max = 80)
        @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Ingresá un correo electrónico válido")
        String correoElectronico,

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

        @NotNull Rol rol,

        @Size(max = 100) String zonaAsignada,
        @Size(max = 20) String patenteVehiculo
) { }