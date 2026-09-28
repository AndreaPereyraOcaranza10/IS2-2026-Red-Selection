package com.tienda.zero.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");

        // Si la cuenta aún no está activa, redirigir directo a la página de activación con el email precargado
        if (exception instanceof DisabledException) {
            String encodedEmail = username != null ? URLEncoder.encode(username.trim(), StandardCharsets.UTF_8) : "";
            response.sendRedirect("/activar?email=" + encodedEmail + "&noActivo=true");
            return;
        }

        // Para contraseñas incorrectas o usuario inexistente, redirigir al login con error
        response.sendRedirect("/login?error=true");
    }
}
