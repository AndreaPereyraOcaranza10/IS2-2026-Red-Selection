package com.example.mascotas.seguridad;

import com.example.mascotas.dto.UsuarioDTO;
import com.example.mascotas.servicios.UsuarioServicio;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginExitosoHandler implements AuthenticationSuccessHandler {

    private final UsuarioServicio usuarioServicio;

    public LoginExitosoHandler(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        UsuarioDetalle detalle = (UsuarioDetalle) authentication.getPrincipal();
        UsuarioDTO dto = usuarioServicio.convertirDTO(detalle.getUsuario());

        request.getSession().setAttribute("usuariosession", dto);

        // Reemplaza la lógica manual de "recordarme" que tenías en el POST /login viejo
        String recordarme = request.getParameter("recordarme");
        if (recordarme != null) {
            Cookie cookie = new Cookie("usuarioRecordado", dto.getId());
            cookie.setMaxAge(60 * 60 * 24 * 2);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }

        response.sendRedirect(request.getContextPath() + "/inicio");
    }
}