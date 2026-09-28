package com.tienda.zero.config;

import com.tienda.zero.service.PerfilService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final PerfilService perfilService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Si es administrador, redirigir al panel de administración
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            response.sendRedirect("/admin");
            return;
        }

        // Si es cliente, verificar si ya completó su información personal de perfil
        String email = authentication.getName();
        if (!perfilService.tienePerfilCompleto(email)) {
            // Se le invita a completar su perfil personal según los requisitos
            response.sendRedirect("/perfil?primeraVez=true");
            return;
        }

        // Si ya completó su perfil, va directo a la tienda
        response.sendRedirect("/shop");
    }
}
