package com.concurso.abm.controller;

import com.concurso.abm.dto.request.AuthRequest;
import com.concurso.abm.dto.response.UsuarioResponse;
import com.concurso.abm.service.UsuarioService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UsuarioService service;
  private final boolean google;

  public AuthController(
      UsuarioService service,
      @Value("${app.google-client-id}") String id,
      @Value("${app.google-client-secret}") String secret) {
    this.service = service;
    google = !id.isBlank() && !secret.isBlank();
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> register(@Valid @RequestBody AuthRequest request) {
    return service.register(request);
  }

  @PostMapping("/login")
  public Map<String, Object> login(@Valid @RequestBody AuthRequest request) {
    return service.login(request);
  }

  @GetMapping("/me")
  public UsuarioResponse me(Principal principal) {
    return service.me(principal.getName());
  }

  @GetMapping("/config")
  public Map<String, Boolean> config() {
    return Map.of("google", google);
  }
}
