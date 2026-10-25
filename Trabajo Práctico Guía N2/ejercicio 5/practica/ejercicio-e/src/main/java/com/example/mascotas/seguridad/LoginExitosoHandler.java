package com.example.mascotas.seguridad;

import com.example.mascotas.dto.UsuarioDTO;
import com.example.mascotas.servicios.UsuarioServicio;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class LoginExitosoHandler implements AuthenticationSuccessHandler {

    private final UsuarioServicio usuarioServicio;
    private final JwtService jwtService;

    public LoginExitosoHandler(UsuarioServicio usuarioServicio, JwtService jwtService) {
        this.usuarioServicio = usuarioServicio;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        UsuarioDetalle detalle = (UsuarioDetalle) authentication.getPrincipal();
        UsuarioDTO dto = usuarioServicio.convertirDTO(detalle.getUsuario());

        request.getSession().setAttribute("usuariosession", dto);

        boolean recordarme = request.getParameter("recordarme") != null;
        long duration = recordarme ? 60 * 60 * 24 * 2 : jwtService.getExpirationSeconds();
        ResponseCookie.ResponseCookieBuilder cookie = ResponseCookie
                .from(JwtService.COOKIE_NAME, jwtService.getToken(detalle, duration))
                .httpOnly(true).secure(request.isSecure()).sameSite("Lax")
                .path(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        if (recordarme) {
            cookie.maxAge(duration);
        }
        response.addHeader("Set-Cookie", cookie.build().toString());

        response.sendRedirect(request.getContextPath() + "/inicio");
    }
}
