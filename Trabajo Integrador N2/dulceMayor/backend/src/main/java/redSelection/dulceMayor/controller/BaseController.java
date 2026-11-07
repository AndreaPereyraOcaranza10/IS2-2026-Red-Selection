package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import redSelection.dulceMayor.service.BaseService;

import java.net.URI;
import java.util.List;

/**
 * PATRÓN: Template Method (capa Controller) + DTO.
 * Fija el esqueleto REST con los verbos y códigos HTTP correctos. La subclase solo declara
 * @RestController, @RequestMapping y cómo obtener el id de la respuesta.
 * Recibe y devuelve únicamente DTOs: nunca entidades JPA.
 */
public abstract class BaseController<REQ, RES> {

    private final BaseService<?, REQ, RES> service;

    protected BaseController(BaseService<?, REQ, RES> service) {
        this.service = service;
    }

    protected abstract String idDe(RES respuesta);

    @PostMapping
    public ResponseEntity<RES> crear(@Valid @RequestBody REQ solicitud) {
        RES creada = service.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(idDe(creada)).toUri();
        return ResponseEntity.created(ubicacion).body(creada);        // 201 + Location
    }

    @GetMapping("/{id}")
    public ResponseEntity<RES> obtener(@PathVariable String id) {
        return ResponseEntity.ok(service.obtener(id));                // 200
    }

    @GetMapping
    public ResponseEntity<List<RES>> listar() {
        return ResponseEntity.ok(service.listar());                   // 200
    }

    @PutMapping("/{id}")
    public ResponseEntity<RES> actualizar(@PathVariable String id, @Valid @RequestBody REQ solicitud) {
        return ResponseEntity.ok(service.actualizar(id, solicitud));  // 200
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();                    // 204
    }
}