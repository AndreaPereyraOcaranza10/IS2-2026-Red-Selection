package redSelection.dulceMayor.dto;

/**
 * Respuesta del login y del cambio de clave. Los nombres de los campos son los que usa el front
 * ({ token, user: { id, employeeId, username, fullName, role, mustChangePassword } }).
 */
public record SesionDTO(String token, UsuarioSesionDTO user) {

    public record UsuarioSesionDTO(
            String id,
            String employeeId,
            String username,
            String fullName,
            String role,
            boolean mustChangePassword
    ) { }
}
