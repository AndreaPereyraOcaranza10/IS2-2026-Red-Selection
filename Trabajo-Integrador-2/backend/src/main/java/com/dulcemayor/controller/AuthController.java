package com.dulcemayor.controller;

import com.dulcemayor.dto.RegistroDTO;
import com.dulcemayor.model.Usuario;
import com.dulcemayor.repository.UsuarioRepository;
import com.dulcemayor.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends ApiControllerTemplate {
    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();
    private final UsuarioService usuarios;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public SessionResponse login(@RequestBody LoginRequest credentials, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = ejecutarEndpoint(() -> authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(credentials.email(), credentials.password())));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        Usuario user = usuarioRepository.findByEmailIgnoreCase(credentials.email()).orElseThrow();
        return new SessionResponse(user.getEmail(), user.getRol().name(), user.getPersona() == null ? "" : user.getPersona().getNombre());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@Valid @RequestBody RegistrationRequest request, HttpServletRequest servletRequest) {
        RegistroDTO dto = new RegistroDTO();
        dto.setEmail(request.email()); dto.setPassword(request.password()); dto.setConfirmPassword(request.password());
        try {
            Usuario user = usuarios.registrarCliente(dto, servletRequest.getScheme() + "://" + servletRequest.getServerName() + ":" + servletRequest.getServerPort());
            return ejecutarEndpoint(() -> new RegistrationResponse(user.getEmail(), "Cuenta creada. Ingresá el código de activación enviado al correo."));
        } catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage()); }
    }

    @PostMapping("/activate")
    public MessageResponse activate(@RequestBody ActivationRequest request) {
        if (!usuarios.activarCuenta(request.email(), request.code())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de activación inválido o vencido");
        return ejecutarEndpoint(() -> new MessageResponse("Cuenta activada"));
    }

    @GetMapping("/me")
    public SessionResponse me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Usuario user = usuarios.buscarPorEmail(authentication.getName());
        return ejecutarEndpoint(() -> new SessionResponse(user.getEmail(), user.getRol().name(), user.getPersona() == null ? "" : user.getPersona().getNombre()));
    }

    @PostMapping("/logout")
    public MessageResponse logout(HttpServletRequest request) {
        if (request.getSession(false) != null) request.getSession(false).invalidate();
        SecurityContextHolder.clearContext();
        return new MessageResponse("Sesión cerrada");
    }

    public record LoginRequest(String email, String password) {}
    public record RegistrationRequest(@jakarta.validation.constraints.Email String email,
                                     @jakarta.validation.constraints.Size(min=6) String password) {}
    public record ActivationRequest(String email, String code) {}
    public record SessionResponse(String email, String role, String displayName) {}
    public record RegistrationResponse(String email, String message) {}
    public record MessageResponse(String message) {}
}
