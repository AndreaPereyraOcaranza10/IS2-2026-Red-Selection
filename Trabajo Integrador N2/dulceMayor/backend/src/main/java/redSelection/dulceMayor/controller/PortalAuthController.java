package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import redSelection.dulceMayor.service.CuentaClienteService;

/** Registro de cuentas del portal. El login se gestiona en AuthController y usa JWT. */
@RestController
@RequestMapping("/api/auth")
public class PortalAuthController {

    private final CuentaClienteService cuentas;

    public PortalAuthController(CuentaClienteService cuentas) {
        this.cuentas = cuentas;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@Valid @RequestBody RegistrationRequest request) {
        String email = cuentas.registrar(request.email(), request.password());
        return new RegistrationResponse(email, "Cuenta creada. Ya podés iniciar sesión y completar los datos de tu perfil.");
    }

    public record RegistrationRequest(@Email @NotBlank String email, @NotBlank @Size(min = 6) String password) {}
    public record RegistrationResponse(String email, String message) {}
}