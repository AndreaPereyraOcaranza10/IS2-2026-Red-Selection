package redselection.ejercicio_c.security;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import redselection.ejercicio_c.dto.AuthDTOs.AuthResponse;
import redselection.ejercicio_c.dto.AuthDTOs.LoginRequest;
import redselection.ejercicio_c.dto.AuthDTOs.RegisterRequest;
import redselection.ejercicio_c.entities.Rol;
import redselection.ejercicio_c.entities.Usuario;
import redselection.ejercicio_c.repositories.UsuarioRepository;

import java.util.Map;

/** Endpoints públicos para obtener un token. Todo lo demás de la API exige ese token. */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            return ResponseEntity.ok(respuesta((Usuario) auth.getPrincipal()));
        } catch (AuthenticationException e) {
            // Mensaje genérico: no revelar si falló el usuario o la contraseña.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuario o contraseña incorrectos."));
        }
    }

    /** El registro público siempre crea usuarios con rol USER. Un ADMIN no se puede auto-asignar. */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "El usuario ya existe."));
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(request.username());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(Rol.USER);
        usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta(usuario));
    }

    private AuthResponse respuesta(Usuario u) {
        return new AuthResponse(jwtService.generarToken(u), "Bearer", u.getUsername(), u.getRol().name(),
                jwtService.getDuracionEnSegundos());
    }
}
