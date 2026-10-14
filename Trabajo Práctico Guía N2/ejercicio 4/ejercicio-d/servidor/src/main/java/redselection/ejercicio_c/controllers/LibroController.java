package redselection.ejercicio_c.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.services.LibroServiceImpl;

import java.util.List;
import java.util.Map;

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


    // Libros del catálogo que no están asignados a ninguna persona
    @GetMapping("/disponibles")
    public ResponseEntity<List<Libro>> listarDisponibles() {
        return ResponseEntity.ok(servicio.listarDisponibles());
    }

    // Igual que el delete base, pero informa el motivo si el libro está asignado a alguien
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            servicio.delete(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "No se pudo eliminar el libro."));
        }
    }
}