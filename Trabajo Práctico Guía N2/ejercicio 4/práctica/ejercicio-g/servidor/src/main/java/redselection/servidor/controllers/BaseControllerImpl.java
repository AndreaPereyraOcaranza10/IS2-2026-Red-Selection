package redselection.servidor.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redselection.servidor.model.Base;
import redselection.servidor.services.BaseService;

import java.util.List;

public abstract class BaseControllerImpl<E extends Base, S extends BaseService<E>> implements BaseController<E> {

    protected final S servicio;

    protected BaseControllerImpl(S servicio) {
        this.servicio = servicio;
    }

    @Override
    @GetMapping("")
    public ResponseEntity<List<E>> getAll() {
        return ResponseEntity.ok(servicio.findAll());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<E> getOne(@PathVariable String id) {
        return ResponseEntity.ok(servicio.findById(id));
    }

    @Override
    @PostMapping("")
    public ResponseEntity<E> save(@RequestBody E entity) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.save(entity));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<E> update(@PathVariable String id, @RequestBody E entity) {
        return ResponseEntity.ok(servicio.update(id, entity));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        servicio.delete(id);
        return ResponseEntity.noContent().build();
    }
}