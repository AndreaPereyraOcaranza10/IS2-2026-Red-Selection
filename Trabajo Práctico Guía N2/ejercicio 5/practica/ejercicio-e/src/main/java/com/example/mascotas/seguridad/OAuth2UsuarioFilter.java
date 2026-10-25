package com.example.mascotas.seguridad;

import com.example.mascotas.servicios.UsuarioServicio;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Adapta la identidad validada a los controladores y vuelve a comprobar bajas y roles.
public class OAuth2UsuarioFilter extends OncePerRequestFilter {
    private final OAuth2UsuarioServicio identidades;
    private final UsuarioServicio usuarios;

    public OAuth2UsuarioFilter(OAuth2UsuarioServicio identidades, UsuarioServicio usuarios) {
        this.identidades = identidades;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String subject = null;
        if (auth instanceof JwtAuthenticationToken jwt) {
            subject = jwt.getToken().getSubject();
        } else if (auth instanceof OAuth2AuthenticationToken oauth && oauth.getPrincipal() instanceof OidcUser oidc) {
            subject = oidc.getSubject();
        }
        if (subject != null) {
            try {
                var local = identidades.buscar(subject);
                if (auth instanceof OAuth2AuthenticationToken oauth) {
                    SecurityContextHolder.getContext().setAuthentication(new OAuth2AuthenticationToken(
                            oauth.getPrincipal(), local.getAuthorities(), oauth.getAuthorizedClientRegistrationId()));
                }
                request.getSession().setAttribute("usuariosession", usuarios.convertirDTO(local.getUsuario()));
            } catch (OAuth2AuthenticationException ex) {
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) {
                    request.getSession(false).invalidate();
                }
                response.sendError(403, "Usuario no vinculado o deshabilitado");
                return;
            }
        } else if (request.getSession(false) != null) {
            request.getSession(false).removeAttribute("usuariosession");
        }
        chain.doFilter(request, response);
    }
}
