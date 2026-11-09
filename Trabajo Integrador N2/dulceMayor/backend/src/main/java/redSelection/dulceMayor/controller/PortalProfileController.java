package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import redSelection.dulceMayor.dto.PerfilClienteDTO;
import redSelection.dulceMayor.dto.PerfilClienteRespuestaDTO;
import redSelection.dulceMayor.service.PerfilClienteService;

/** Perfil del cliente en el portal. Solo delega: las reglas y la persistencia están en PerfilClienteService. */
@RestController
@RequestMapping("/api/profile")
public class PortalProfileController {

    private final PerfilClienteService perfilService;

    public PortalProfileController(PerfilClienteService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public PerfilClienteRespuestaDTO profile(Authentication authentication) {
        return perfilService.obtener(authentication.getName());
    }

    @PutMapping
    public PerfilClienteRespuestaDTO update(Authentication authentication, @Valid @RequestBody PerfilClienteDTO request) {
        return perfilService.actualizar(authentication.getName(), request);
    }
}