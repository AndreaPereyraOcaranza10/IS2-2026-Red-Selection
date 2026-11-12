package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.dto.Auth0CredentialDTO;
import redSelection.dulceMayor.dto.SesionDTO;
import redSelection.dulceMayor.service.Auth0AuthService;

@RestController
@RequestMapping("/api/auth")
public class Auth0AuthController {

    private final Auth0AuthService auth0AuthService;

    public Auth0AuthController(Auth0AuthService auth0AuthService) {
        this.auth0AuthService = auth0AuthService;
    }

    @PostMapping("/auth0")
    public SesionDTO ingresarConAuth0(@Valid @RequestBody Auth0CredentialDTO request) {
        return auth0AuthService.ingresar(request.idToken());
    }
}
