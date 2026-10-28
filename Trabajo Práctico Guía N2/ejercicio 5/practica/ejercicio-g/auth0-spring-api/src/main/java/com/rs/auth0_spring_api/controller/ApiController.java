package com.rs.auth0_spring_api.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    // Endpoint público (Home.tsx)
    @GetMapping("/public")
    public Map<String, String> publicEndpoint() {
        return Map.of("message", "Este es un endpoint público. Lo puedes ver sin estar autenticado.");
    }

    // Endpoint protegido para clientes (ClientPage.tsx)
    @GetMapping("/client")
    public Map<String, String> clientEndpoint() {
        return Map.of("message", "Este es un endpoint de usuario/cliente. Puedes ver esta respuesta porque tienes el rol de cliente.");
    }

    // Endpoint protegido para administradores (AdminPage.tsx)
    @GetMapping("/admin")
    public Map<String, String> adminEndpoint() {
        return Map.of("message", "Este es un endpoint de administrador. Puedes ver esta respuesta porque eres administrador.");
    }
}