package redselection.ejercicio_c.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * (Se instancia en SecurityConfig, no es @Component, para que corra solo dentro de la cadena de Spring Security.)
 * Se ejecuta en CADA request antes de llegar al controller:
 *   1. Lee el header "Authorization: Bearer <token>".
 *   2. Si el token es válido, carga el usuario y lo deja autenticado en el SecurityContext.
 *   3. Si no hay token o es inválido, NO autentica: más adelante Spring Security responde 401.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(PREFIJO.length());

            jwtService.validarYObtenerUsername(token).ifPresent(username -> {
                try {
                    // Se consulta la BD para que un usuario borrado/cambiado de rol pierda acceso enseguida.
                    UserDetails usuario = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (UsernameNotFoundException ignorado) {
                    // usuario inexistente: queda sin autenticar -> 401
                }
            });
        }

        chain.doFilter(request, response);
    }
}
