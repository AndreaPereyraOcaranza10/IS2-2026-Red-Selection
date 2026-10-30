package com.concurso.abm.security;

import com.concurso.abm.entity.Usuario;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.time.Instant;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final JwtEncoder encoder;
  private final JwtDecoder decoder;

  public JwtService(@Value("${app.jwt-secret}") String secret) {
    if (secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32)
      throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 bytes");
    var key =
        new SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
    encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
    var d = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    d.setJwtValidator(JwtValidators.createDefaultWithIssuer("abm"));
    decoder = d;
  }

  public String issue(Usuario u) {
    Instant now = Instant.now();
    return encoder
        .encode(
            JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder()
                    .issuer("abm")
                    .subject(u.getId().toString())
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(1800))
                    .claim("ver", u.getCredentialVersion())
                    .build()))
        .getTokenValue();
  }

  public Jwt decode(String token) {
    return decoder.decode(token);
  }
}
