package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import redSelection.dulceMayor.enums.ProveedorAuth;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.model.Usuario;
import redSelection.dulceMayor.repository.UsuarioRepository;

import java.util.Locale;

/** Registro de cuentas del portal. El login se gestiona en AuthController y usa JWT. */
@RestController
@RequestMapping("/api/auth")
public class PortalAuthController {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public PortalAuthController(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@Valid @RequestBody RegistrationRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.findByNombreUsuarioIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
        }
        Usuario usuario = Usuario.builder()
                .nombreUsuario(email)
                .clave(passwordEncoder.encode(request.password()))
                .rol(Rol.CLIENTE)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .activo(true)
                .primerIngreso(false)
                .eliminado(false)
                .build();
        usuarios.save(usuario);
        return new RegistrationResponse(email,
                "Cuenta creada. Ya podés iniciar sesión y completar los datos de tu perfil.");
    }

    public record RegistrationRequest(@Email @NotBlank String email, @NotBlank @Size(min = 6) String password) {}
    public record RegistrationResponse(String email, String message) {}
}
