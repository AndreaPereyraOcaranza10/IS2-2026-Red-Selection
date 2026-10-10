package com.redselection.clima.controller;

import com.redselection.clima.service.ClimaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*") // Clave para evitar problemas de CORS cuando conectes React
@RequestMapping("/ciudad")
public class ClimaController {

    private final ClimaService service;

    public ClimaController(ClimaService service) {
        this.service = service;
    }

    @GetMapping("/clima/{ciudad}")
    public ResponseEntity<String> obtenerClima(@PathVariable String ciudad) {
        return service.obtenerClimaApi(ciudad);
    }
}