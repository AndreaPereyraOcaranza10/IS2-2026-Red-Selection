package redSelection.dulceMayor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import redSelection.dulceMayor.dto.SesionDTO;
import redSelection.dulceMayor.enums.ProveedorAuth;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Usuario;
import redSelection.dulceMayor.repository.UsuarioRepository;
import redSelection.dulceMayor.security.JwtService;

import java.util.Locale;

/** Valida una identidad OIDC de Auth0 y la intercambia por una sesión propia de Dulce Mayor. */
@Service
public class Auth0AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final String clientId;
    private final JwtDecoder auth0Decoder;

    public Auth0AuthService(UsuarioRepository usuarioRepository,
                           JwtService jwtService,
                           @Value("${app.auth.auth0.domain:}") String configuredDomain,
                           @Value("${app.auth.auth0.client-id:}") String configuredClientId) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.clientId = configuredClientId == null ? "" : configuredClientId.trim();
        String domain = normalizeDomain(configuredDomain);
        if (domain.isBlank() || this.clientId.isBlank()) {
            this.auth0Decoder = null;
            return;
        }

        String issuer = "https://" + domain + "/";
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(issuer + ".well-known/jwks.json").build();
        OAuth2TokenValidator<Jwt> auth0Claims = jwt -> {
            boolean validAudience = jwt.getAudience().contains(this.clientId);
            boolean verifiedEmail = Boolean.TRUE.equals(jwt.getClaim("email_verified"));
            boolean hasIdentity = jwt.getSubject() != null && !jwt.getSubject().isBlank()
                    && jwt.getClaimAsString("email") != null && !jwt.getClaimAsString("email").isBlank();
            if (validAudience && verifiedEmail && hasIdentity) return OAuth2TokenValidatorResult.success();
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "La identidad de Auth0 no es valida.", null));
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer), auth0Claims));
        this.auth0Decoder = decoder;
    }

    @Transactional
    public SesionDTO ingresar(String idToken) {
        if (auth0Decoder == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El acceso con Google via Auth0 no esta configurado. Define AUTH0_DOMAIN y AUTH0_CLIENT_ID.");
        }
        if (idToken == null || idToken.isBlank() || idToken.length() > 8192) {
            throw new BadCredentialsException("La credencial de Auth0 no es valida.");
        }

        Jwt identity;
        try {
            identity = auth0Decoder.decode(idToken);
        } catch (JwtException ex) {
            throw new BadCredentialsException("La credencial de Auth0 no es valida.", ex);
        }

        String subject = identity.getSubject();
        String email = identity.getClaimAsString("email").trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.findByProveedorAuthAndIdExterno(ProveedorAuth.AUTH0, subject)
                .orElseGet(() -> registrarOVincularCuenta(email, subject));

        if (!usuario.isActivo() || usuario.isEliminado() || usuario.getRol() != Rol.CLIENTE) {
            throw new BadCredentialsException("La cuenta no esta habilitada para el portal de clientes.");
        }
        usuario.registrarAcierto();
        usuarioRepository.save(usuario);

        String nombre = identity.getClaimAsString("name");
        if (nombre == null || nombre.isBlank()) nombre = email;
        return new SesionDTO(jwtService.emitir(usuario, null), new SesionDTO.UsuarioSesionDTO(
                usuario.getId(), null, usuario.getNombreUsuario(), nombre, usuario.getRol().name(), false));
    }

    private Usuario registrarOVincularCuenta(String email, String subject) {
        Usuario existente = usuarioRepository.findByNombreUsuarioIgnoreCase(email).orElse(null);
        if (existente != null) {
            // Mantiene las cuentas creadas por el anterior acceso directo de Google al pasar a Auth0.
            if (existente.getProveedorAuth() == ProveedorAuth.GOOGLE && existente.getRol() == Rol.CLIENTE) {
                existente.setProveedorAuth(ProveedorAuth.AUTH0);
                existente.setIdExterno(subject);
                return usuarioRepository.save(existente);
            }
            throw new ReglaNegocioException("Ya existe una cuenta con ese correo. Inicia sesion con correo y clave.");
        }
        return usuarioRepository.save(Usuario.builder()
                .nombreUsuario(email)
                .clave(null)
                .rol(Rol.CLIENTE)
                .proveedorAuth(ProveedorAuth.AUTH0)
                .idExterno(subject)
                .primerIngreso(false)
                .activo(true)
                .eliminado(false)
                .build());
    }

    private static String normalizeDomain(String configuredDomain) {
        if (configuredDomain == null || configuredDomain.isBlank()) return "";
        String domain = configuredDomain.trim().replaceFirst("^https?://", "").replaceAll("/+$", "");
        if (!domain.matches("[A-Za-z0-9.-]+")) {
            throw new IllegalArgumentException("AUTH0_DOMAIN debe ser solo el dominio del tenant Auth0.");
        }
        return domain.toLowerCase(Locale.ROOT);
    }
}
