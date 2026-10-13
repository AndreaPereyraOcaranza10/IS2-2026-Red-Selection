package redselection.ejercicio_c.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.services.LibroServiceImpl;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api/v1/libros")
public class LibroController
        extends BaseControllerImpl<Libro, LibroServiceImpl> {

    @GetMapping("/autor/{autorId}")
    public ResponseEntity<List<Libro>> listarPorAutor(
            @PathVariable Long autorId) {

        return ResponseEntity.ok(
                servicio.listarPorAutor(autorId)
        );
    }
}