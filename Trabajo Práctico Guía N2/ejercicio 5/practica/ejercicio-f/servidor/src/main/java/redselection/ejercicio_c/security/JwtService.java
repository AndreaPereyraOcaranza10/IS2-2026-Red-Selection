package redselection.ejercicio_c.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Genera y valida los tokens JWT (firmados con HMAC-SHA256).
 * La clave secreta NUNCA va en el código: se lee de la variable de entorno JWT_SECRET (archivo .env).
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration duracion;

    public JwtService(@Value("${security.jwt.secret}") String secreto,
                      @Value("${security.jwt.expiration-minutes:60}") long minutos) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            // HS256 exige una clave de al menos 256 bits; si no, fallamos al arrancar (fail fast).
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.duracion = Duration.ofMinutes(minutos);
    }

    public String generarToken(UserDetails usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("rol", usuario.getAuthorities().iterator().next().getAuthority())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracion)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el username si el token es válido (firma correcta y no vencido); vacío en cualquier otro caso. */
    public Optional<String> validarYObtenerUsername(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long getDuracionEnSegundos() {
        return duracion.toSeconds();
    }
}
