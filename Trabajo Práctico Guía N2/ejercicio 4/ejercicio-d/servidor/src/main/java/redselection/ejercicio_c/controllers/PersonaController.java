package redselection.ejercicio_c.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redselection.ejercicio_c.dto.PersonaDTO;
import redselection.ejercicio_c.entities.Persona;
import redselection.ejercicio_c.services.PersonaServiceImpl;

import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api/v1/personas")
public class PersonaController extends BaseControllerImpl<Persona, PersonaDTO, PersonaServiceImpl>{
    @GetMapping("/search")
    public ResponseEntity<?> search (@RequestParam String filtro) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(mapper.toDtoList(servicio.search(filtro)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(("{\"error\":" + e.getMessage() + "}"));
        }
    }

    // Asigna a la persona un libro que ya existe en el catálogo (no crea ningún libro)
    @PostMapping("/{personaId}/libros/{libroId}")
    public ResponseEntity<?> asignarLibro(
            @PathVariable Long personaId,
            @PathVariable Long libroId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDevolucion) {
        try {
            return ResponseEntity.ok(mapper.toDto(servicio.asignarLibro(personaId, libroId, fechaDevolucion)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "No se pudo asignar el libro."));
        }
    }

    // Desasigna el libro de la persona (no lo elimina: sigue en el catálogo)
    @DeleteMapping("/{personaId}/libros/{libroId}")
    public ResponseEntity<?> desasignarLibro(
            @PathVariable Long personaId,
            @PathVariable Long libroId) {
        try {
            servicio.desasignarLibro(personaId, libroId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "No se pudo desasignar el libro."));
        }
    }
}