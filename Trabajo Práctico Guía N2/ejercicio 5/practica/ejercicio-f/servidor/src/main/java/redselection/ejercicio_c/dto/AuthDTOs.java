package redselection.ejercicio_c.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTOs del login/registro. Se usan DTOs (y no la entidad Usuario) para no exponer el hash de la contraseña. */
public final class AuthDTOs {

    private AuthDTOs() {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Size(min = 8, max = 100) String password) {
    }

    public record AuthResponse(String token, String tipo, String username, String rol, long expiraEnSegundos) {
    }
}
