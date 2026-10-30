package com.concurso.abm.controller;

import com.concurso.abm.dto.request.*;
import com.concurso.abm.dto.response.UsuarioResponse;
import com.concurso.abm.service.UsuarioService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios/me")
public class UsuarioController {
  private final UsuarioService service;

  public UsuarioController(UsuarioService service) {
    this.service = service;
  }

  @GetMapping
  public UsuarioResponse me(Principal p) {
    return service.me(p.getName());
  }

  @PutMapping
  public UsuarioResponse update(Principal p, @Valid @RequestBody PerfilRequest r) {
    return service.update(p.getName(), r);
  }

  @PutMapping("/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void password(Principal p, @Valid @RequestBody PasswordRequest r) {
    service.password(p.getName(), r);
  }
}
