package com.example.mascotas.controladores;

import com.example.mascotas.dto.UsuarioDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("oauth2")
public class OAuth2CuentaController {
    @GetMapping("/api/me")
    public UsuarioDTO cuenta(HttpSession session) {
        return (UsuarioDTO) session.getAttribute("usuariosession");
    }
}
