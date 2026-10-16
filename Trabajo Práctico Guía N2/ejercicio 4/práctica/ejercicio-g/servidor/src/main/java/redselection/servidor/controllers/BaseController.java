package redselection.servidor.controllers;

import org.springframework.http.ResponseEntity;
import redselection.servidor.model.Base;

import java.util.List;

public interface BaseController<E extends Base> {
    ResponseEntity<List<E>> getAll();
    ResponseEntity<E> getOne(String id);
    ResponseEntity<E> save(E entity);
    ResponseEntity<E> update(String id, E entity);
    ResponseEntity<Void> delete(String id);
}
