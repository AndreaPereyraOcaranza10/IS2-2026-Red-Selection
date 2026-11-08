package redSelection.dulceMayor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Lee "Authorization: Bearer <token>", valida el JWT y deja al usuario autenticado para el resto de la cadena.
 * Un token inválido no corta nada acá: el pedido sigue sin autenticar y Spring Security responde 401 si el
 * endpoint lo exige. Regla de negocio: mientras el usuario tenga la clave inicial, solo puede usar /api/auth/**.
 * No es un @Component a propósito: lo crea SecurityConfig para que corra una sola vez, dentro de la cadena de seguridad.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ControlDeSesion controlDeSesion;

    public JwtAuthFilter(JwtService jwtService, ControlDeSesion controlDeSesion) {
        this.jwtService = jwtService;
        this.controlDeSesion = controlDeSesion;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            Optional<UsuarioAutenticado> encontrado = jwtService.validar(cabecera.substring(7).trim());
            // Además de la firma y el vencimiento, el usuario tiene que seguir activo y no haber revocado el token
            if (encontrado.isPresent() && controlDeSesion.estaVigente(encontrado.get())) {
                UsuarioAutenticado usuario = encontrado.get();
                if (usuario.isPrimerIngreso() && !request.getRequestURI().startsWith("/api/auth/")) {
                    SecurityJson.responder(response, 403, "Forbidden",
                            "Tenés que cambiar tu clave antes de continuar.", "PASSWORD_CHANGE_REQUIRED");
                    return;
                }
                SecurityContextHolder.getContext().setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                usuario, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))));
            }
        }
        chain.doFilter(request, response);
    }
}
