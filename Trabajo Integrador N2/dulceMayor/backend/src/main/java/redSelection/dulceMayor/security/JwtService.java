package redSelection.dulceMayor.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.model.Usuario;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Emite y valida los tokens JWT (HS256, firmados con app.jwt.secret). No guarda nada: el token trae
 * quién es el usuario, su rol y si todavía debe cambiar la clave inicial.
 * Usa las clases de Spring Security / Nimbus que ya vienen con el starter de OAuth2 (no suma dependencias).
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String SECRETO_DE_DESARROLLO = "dulce-mayor-clave-solo-para-desarrollo-no-usar-en-produccion";

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final Duration validez;

    public JwtService(@Value("${app.jwt.secret:" + SECRETO_DE_DESARROLLO + "}") String secreto,
                      @Value("${app.jwt.horas-validez:12}") long horasValidez,
                      Environment entorno) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret debe tener al menos 32 caracteres (firma HS256).");
        }
        if (SECRETO_DE_DESARROLLO.equals(secreto) && entorno.acceptsProfiles(Profiles.of("prod"))) {
            throw new IllegalStateException("En producción JWT_SECRET es obligatorio y no puede ser el secreto de desarrollo.");
        }
        if (SECRETO_DE_DESARROLLO.equals(secreto)) {
            log.warn("Se está usando el secreto JWT de desarrollo. En producción definí JWT_SECRET.");
        }
        SecretKey clave = new SecretKeySpec(bytes, "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(clave));
        this.decoder = NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
        this.validez = Duration.ofHours(horasValidez);
    }

    public String emitir(Usuario usuario, String empleadoId) {
        Instant ahora = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("dulce-mayor")
                .subject(usuario.getId())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(validez))
                .claim("usuario", usuario.getNombreUsuario())
                .claim("rol", usuario.getRol().name())
                .claim("primerIngreso", usuario.isPrimerIngreso())
                .claim("ver", usuario.getVersionToken());
        if (empleadoId != null) {
            claims.claim("empleadoId", empleadoId);
        }
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(cabecera, claims.build())).getTokenValue();
    }

    /** Devuelve el usuario del token, o vacío si está vencido, mal firmado o mal formado. */
    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            Boolean primerIngreso = jwt.getClaim("primerIngreso");
            Number version = jwt.getClaim("ver");   // un token sin versión (anterior a este cambio) queda invalidado
            return Optional.of(new UsuarioAutenticado(
                    jwt.getSubject(),
                    jwt.getClaimAsString("usuario"),
                    Rol.valueOf(jwt.getClaimAsString("rol")),
                    Boolean.TRUE.equals(primerIngreso),
                    jwt.getClaimAsString("empleadoId"),
                    version == null ? -1 : version.intValue()));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
