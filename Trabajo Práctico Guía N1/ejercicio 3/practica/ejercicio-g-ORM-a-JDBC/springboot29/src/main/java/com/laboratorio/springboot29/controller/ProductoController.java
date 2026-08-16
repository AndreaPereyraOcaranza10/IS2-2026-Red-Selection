package com.laboratorio.springboot29.controller;

import com.laboratorio.springboot29.dto.ProductoRequest;
import com.laboratorio.springboot29.dto.ProductoResponse;
import com.laboratorio.springboot29.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {
    private final ProductoService productoService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> findById(@PathVariable("id") Integer id) {
        Optional<ProductoResponse> response = this.productoService.findById(id);
        return response.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> findAll() {
        return ResponseEntity.ok(this.productoService.findAll());
    }

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody ProductoRequest request) {
        this.productoService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<Boolean> update(@PathVariable("id") Integer id, @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(this.productoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(this.productoService.delete(id));
    }
}