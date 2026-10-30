package com.concurso.abm.patterns.strategy;

import com.concurso.abm.entity.Usuario;
import com.concurso.abm.exception.DomainException;
import com.concurso.abm.repository.UsuarioRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GoogleIdentityStrategy implements IdentityStrategy {
  private final UsuarioRepository users;

  public GoogleIdentityStrategy(UsuarioRepository users) {
    this.users = users;
  }

  // Called only with email/sub from a successfully verified OIDC principal.
  @Transactional
  public Usuario authenticate(String email, String subject) {
    Usuario existing =
        users.findByProveedorAutenticacionAndProveedorId("GOOGLE", subject).orElse(null);
    if (existing != null) {
      if (!existing.getActivo()) throw DomainException.invalid("Cuenta desactivada");
      return existing;
    }
    String normalized = email.strip().toLowerCase(java.util.Locale.ROOT);
    if (users.findByEmail(normalized).isPresent())
      throw DomainException.conflict(
          "El email ya tiene una cuenta. Iniciá sesión con su método original.");
    Usuario u = new Usuario();
    u.setEmail(normalized);
    u.setProveedorAutenticacion("GOOGLE");
    u.setProveedorId(subject);
    return users.saveAndFlush(u);
  }
}
