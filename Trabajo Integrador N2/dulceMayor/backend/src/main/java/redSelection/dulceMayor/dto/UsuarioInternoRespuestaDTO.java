package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.enums.TipoDocumento;

/** La respuesta nunca incluye la contraseña ni su hash. */
public record UsuarioInternoRespuestaDTO(
        String id,
        String nombreUsuario,
        String nombre,
        String apellido,
        TipoDocumento tipoDocumento,
        String documentoParcial,
        Rol rol,
        boolean activo,
        boolean primerIngreso
) { }
