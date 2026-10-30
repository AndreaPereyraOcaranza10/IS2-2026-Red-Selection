package com.concurso.abm.controller;

import com.concurso.abm.dto.response.CatalogoGeograficoResponse;
import com.concurso.abm.service.CatalogoGeograficoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalogos/geografia")
public class CatalogoGeograficoController {
  private final CatalogoGeograficoService service;

  public CatalogoGeograficoController(CatalogoGeograficoService service) {
    this.service = service;
  }

  @GetMapping
  public CatalogoGeograficoResponse obtener() {
    return service.obtener();
  }
}
