package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.dto.CambioClaveDTO;
import redSelection.dulceMayor.dto.CambioClaveUsuarioDTO;
import redSelection.dulceMayor.dto.LoginDTO;
import redSelection.dulceMayor.dto.SesionDTO;
import redSelection.dulceMayor.security.UsuarioAutenticado;
import redSelection.dulceMayor.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 200 con el token; 401 si las credenciales no son válidas. */
    @PostMapping("/login")
    public ResponseEntity<SesionDTO> login(@Valid @RequestBody LoginDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    /** Cierra la sesión en el servidor: el token deja de ser válido aunque no haya vencido. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        authService.cerrarSesion(usuario);
        return ResponseEntity.noContent().build();
    }

    /** Cambio obligatorio de la clave inicial. Devuelve una sesión nueva (sin primerIngreso). */
    @PostMapping("/change-password")
    public ResponseEntity<SesionDTO> cambiarClave(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                   @Valid @RequestBody CambioClaveUsuarioDTO dto) {
        return ResponseEntity.ok(authService.cambiarClave(usuario, dto));
    }

    @PostMapping("/first-login/change-password")
    public ResponseEntity<SesionDTO> cambiarClaveInicial(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                         @Valid @RequestBody CambioClaveDTO dto) {
        return ResponseEntity.ok(authService.cambiarClaveInicial(usuario, dto));
    }
}
