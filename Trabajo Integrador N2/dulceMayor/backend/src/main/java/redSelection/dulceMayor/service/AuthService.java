package redSelection.dulceMayor.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.CambioClaveDTO;
import redSelection.dulceMayor.dto.LoginDTO;
import redSelection.dulceMayor.dto.SesionDTO;
import redSelection.dulceMayor.exception.CuentaBloqueadaException;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Persona;
import redSelection.dulceMayor.model.Usuario;
import redSelection.dulceMayor.repository.PersonaRepository;
import redSelection.dulceMayor.repository.UsuarioRepository;
import redSelection.dulceMayor.security.JwtService;
import redSelection.dulceMayor.security.UsuarioAutenticado;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Login y cambio de clave. Regla de negocio de la consigna: todo usuario nuevo del sistema administrativo
 * nace con la clave "dulcemayor" y primerIngreso = true; hasta que la cambie, su token solo sirve
 * para /api/auth/** (lo hace cumplir JwtAuthFilter).
 */
@Service
public class AuthService {

    public static final String CLAVE_INICIAL = "dulcemayor";

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final int maxIntentos;
    private final Duration duracionBloqueo;

    public AuthService(UsuarioRepository usuarioRepository,
                       PersonaRepository personaRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       @Value("${app.seguridad.max-intentos:5}") int maxIntentos,
                       @Value("${app.seguridad.minutos-bloqueo:15}") long minutosBloqueo) {
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.maxIntentos = maxIntentos;
        this.duracionBloqueo = Duration.ofMinutes(minutosBloqueo);
    }

    /**
     * noRollbackFor: al fallar el login se lanza una excepción, pero el contador de intentos fallidos
     * TIENE que quedar guardado; sin esto la transacción haría rollback y el bloqueo nunca se activaría.
     */
    @Transactional(noRollbackFor = AuthenticationException.class)
    public SesionDTO login(LoginDTO dto) {
        LocalDateTime ahora = LocalDateTime.now();
        Usuario usuario = usuarioRepository.findByNombreUsuarioIgnoreCase(dto.username().trim())
                .filter(u -> !u.isEliminado())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        // Bloqueada: ni se mira la clave, así un atacante no puede seguir probando durante el bloqueo.
        if (usuario.estaBloqueado(ahora)) {
            throw new CuentaBloqueadaException(Duration.between(ahora, usuario.getBloqueadoHasta()).toSeconds());
        }
        // Un cliente que entra con Google no tiene clave local: no puede ingresar por acá.
        if (usuario.getClave() == null || !passwordEncoder.matches(dto.password(), usuario.getClave())) {
            usuario.registrarFallo(maxIntentos, duracionBloqueo, ahora);
            usuarioRepository.save(usuario);
            if (usuario.estaBloqueado(ahora)) {
                throw new CuentaBloqueadaException(duracionBloqueo.toSeconds());
            }
            throw new BadCredentialsException("Credenciales inválidas");
        }
        if (!usuario.isActivo()) {
            throw new DisabledException("Tu usuario está desactivado. Consultá con administración.");
        }
        usuario.registrarAcierto();
        usuarioRepository.save(usuario);
        return sesion(usuario);
    }

    /** Logout real: sube la versión de token del usuario, con lo que todos sus JWT emitidos dejan de valer. */
    @Transactional
    public void cerrarSesion(UsuarioAutenticado actual) {
        usuarioRepository.findByIdAndEliminadoFalse(actual.getId()).ifPresent(u -> {
            u.revocarTokens();
            usuarioRepository.save(u);
        });
    }

    @Transactional
    public SesionDTO cambiarClaveInicial(UsuarioAutenticado actual, CambioClaveDTO dto) {
        Usuario usuario = usuarioRepository.findByIdAndEliminadoFalse(actual.getId())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        if (!usuario.isPrimerIngreso()) {
            throw new ReglaNegocioException("Tu clave ya fue cambiada.");
        }
        validarClaveNueva(dto.newPassword());
        usuario.setClave(passwordEncoder.encode(dto.newPassword()));
        usuario.setPrimerIngreso(false);
        usuario.revocarTokens();   // el token del primer ingreso (con la clave inicial) deja de servir
        usuarioRepository.save(usuario);
        return sesion(usuario);
    }

    /** Mismas reglas que muestra el front: 8+ caracteres, mayúscula y número, un símbolo, distinta de la inicial. */
    private void validarClaveNueva(String clave) {
        if (clave.length() < 8) {
            throw new ReglaNegocioException("La clave debe tener al menos 8 caracteres.");
        }
        if (!clave.chars().anyMatch(Character::isUpperCase) || !clave.chars().anyMatch(Character::isDigit)) {
            throw new ReglaNegocioException("La clave debe incluir una mayúscula y un número.");
        }
        if (clave.chars().allMatch(Character::isLetterOrDigit)) {
            throw new ReglaNegocioException("La clave debe incluir un símbolo (#, $, !, ?).");
        }
        if (CLAVE_INICIAL.equalsIgnoreCase(clave)) {
            throw new ReglaNegocioException("La clave nueva no puede ser la clave inicial.");
        }
    }

    private SesionDTO sesion(Usuario usuario) {
        Optional<Persona> persona = personaRepository.findByUsuarioId(usuario.getId());
        String empleadoId = persona.map(Persona::getId).orElse(null);
        String nombreCompleto = persona.map(p -> p.getNombre() + " " + p.getApellido()).orElse(usuario.getNombreUsuario());
        return new SesionDTO(
                jwtService.emitir(usuario, empleadoId),
                new SesionDTO.UsuarioSesionDTO(
                        usuario.getId(),
                        empleadoId,
                        usuario.getNombreUsuario(),
                        nombreCompleto,
                        usuario.getRol().name(),
                        usuario.isPrimerIngreso()));
    }
}
