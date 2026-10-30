package com.concurso.abm.patterns.strategy;

import com.concurso.abm.entity.Usuario;
import com.concurso.abm.exception.DomainException;
import com.concurso.abm.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class LocalIdentityStrategy implements IdentityStrategy {
  private final UsuarioRepository users;
  private final PasswordEncoder encoder;

  public LocalIdentityStrategy(UsuarioRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  public Usuario authenticate(String email, String password) {
    Usuario u = users.findByEmail(email.strip().toLowerCase(java.util.Locale.ROOT)).orElse(null);
    String hash =
        u != null && u.getPasswordHash() != null
            ? u.getPasswordHash()
            : "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
    boolean matches = encoder.matches(password, hash);
    if (u == null || !u.getActivo() || u.getPasswordHash() == null || !matches)
      throw new DomainException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
    return u;
  }
}
