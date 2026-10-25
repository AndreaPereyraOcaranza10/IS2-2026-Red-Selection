package com.example.mascotas.controladores;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class LoginModeloAdvice {
    private final Environment environment;

    public LoginModeloAdvice(Environment environment) { this.environment = environment; }

    @ModelAttribute("oauth2Activo")
    public boolean oauth2Activo() { return environment.acceptsProfiles(Profiles.of("oauth2")); }
}
