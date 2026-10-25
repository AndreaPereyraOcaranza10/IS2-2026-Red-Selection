package com.example.mascotas.seguridad;

import com.example.mascotas.servicios.UsuarioServicio;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UsuarioDetalleServicio usuarios;
    private final UsuarioServicio usuarioServicio;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioDetalleServicio usuarios,
                                   UsuarioServicio usuarioServicio) {
        this.jwtService = jwtService;
        this.usuarios = usuarios;
        this.usuarioServicio = usuarioServicio;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // La sesión sólo conserva datos de las vistas; nunca acredita la identidad.
        if (request.getSession(false) != null) {
            request.getSession(false).removeAttribute("usuariosession");
        }
        String header = request.getHeader("Authorization");
        boolean bearer = header != null && header.startsWith("Bearer ");
        String token = bearer ? header.substring(7) : null;
        if (!bearer && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (JwtService.COOKIE_NAME.equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token != null) {
            try {
                UsuarioDetalle user = (UsuarioDetalle) usuarios.loadUserByUsername(
                        jwtService.getUsernameFromToken(token));
                if (!user.isEnabled()) {
                    throw new UsernameNotFoundException("Usuario deshabilitado");
                }
                var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                request.getSession().setAttribute("usuariosession",
                        usuarioServicio.convertirDTO(user.getUsuario()));
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
                SecurityContextHolder.clearContext();
                if (bearer) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o vencido");
                    return;
                }
                // Una cookie vencida no impide volver a iniciar sesión.
                Cookie cookie = new Cookie(JwtService.COOKIE_NAME, "");
                cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                cookie.setMaxAge(0);
                cookie.setHttpOnly(true);
                response.addCookie(cookie);
            }
        }
        chain.doFilter(request, response);
    }
}
