package com.dulcemayor.controller;

import com.dulcemayor.dto.ProductResponseDTO;
import com.dulcemayor.service.CatalogoMayoristaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogoRestController extends ApiControllerTemplate {
    private final CatalogoMayoristaService catalogo;

    @GetMapping("/products")
    public List<ProductResponseDTO> products(@RequestParam(required = false) String search,
                                              @RequestParam(required = false) String category) {
        return ejecutarEndpoint(() -> catalogo.listar(search, category, false));
    }

    @GetMapping("/products/{id}")
    public ProductResponseDTO product(@PathVariable String id) { return ejecutarEndpoint(() -> catalogo.buscar(id)); }

    @GetMapping("/categories")
    public List<String> categories() { return ejecutarEndpoint(catalogo::categorias); }

    @GetMapping("/health")
    public HealthResponse health() { return ejecutarEndpoint(() -> new HealthResponse("ok", "Dulce Mayor API", "1.0")); }

    public record HealthResponse(String status, String application, String version) {}
}
