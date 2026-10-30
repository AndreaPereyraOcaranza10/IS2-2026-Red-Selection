package com.concurso.abm.security;

import com.concurso.abm.repository.UsuarioRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {
  private final JwtService jwt;
  private final UsuarioRepository users;

  public JwtFilter(JwtService jwt, UsuarioRepository users) {
    this.jwt = jwt;
    this.users = users;
  }

  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String auth = request.getHeader("Authorization");
    if (auth != null && auth.startsWith("Bearer ")) {
      try {
        var token = jwt.decode(auth.substring(7));
        var u = users.findById(Long.valueOf(token.getSubject())).orElseThrow();
        Number version = token.getClaim("ver");
        if (!u.getActivo() || version == null || u.getCredentialVersion() != version.longValue())
          throw new IllegalArgumentException();
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
            new UsernamePasswordAuthenticationToken(
                u.getId().toString(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol()))));
        SecurityContextHolder.setContext(context);
      } catch (Exception e) {
        SecurityContextHolder.clearContext();
        response.setStatus(401);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response
            .getWriter()
            .write("{\"message\":\"Sesión inválida o vencida. Iniciá sesión nuevamente.\"}");
        return;
      }
    }
    chain.doFilter(request, response);
  }
}
