package com.dulcemayor.controller;

import com.dulcemayor.dto.PerfilDTO;
import com.dulcemayor.service.PerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class PerfilController extends ApiControllerTemplate {

    private final PerfilService perfilService;

    @GetMapping
    public PerfilDTO profile(Authentication authentication) {
        return ejecutarEndpoint(() -> perfilService.obtenerPerfilUsuario(authentication.getName()));
    }

    @PutMapping
    public PerfilDTO updateProfile(Authentication authentication, @RequestBody PerfilDTO profile) {
        return ejecutarEndpoint(() -> {
            perfilService.guardarPerfilUsuario(authentication.getName(), profile);
            return perfilService.obtenerPerfilUsuario(authentication.getName());
        });
    }
}
