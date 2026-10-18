package redselection.ejercicio_c.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redselection.ejercicio_c.dto.LibroDTO;
import redselection.ejercicio_c.dto.LibroSolicitud;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.patrones.adaptador.LibroMapper;
import redselection.ejercicio_c.patrones.fabrica.TipoLibro;
import redselection.ejercicio_c.services.LibroServiceImpl;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api/v1/libros")
public class LibroController
        extends BaseControllerImpl<Libro, LibroDTO, LibroServiceImpl> {

    // El mismo mapper que usa la clase base, pero con el método extra toSolicitud
    @Autowired
    private LibroMapper libroMapper;

    @GetMapping("/autor/{autorId}")
    public ResponseEntity<List<LibroDTO>> listarPorAutor(
            @PathVariable Long autorId) {

        return ResponseEntity.ok(
                mapper.toDtoList(servicio.listarPorAutor(autorId))
        );
    }

    // Búsqueda por título, género o autor: GET /api/v1/libros/buscar?criterio=TITULO&valor=dune
    @GetMapping("/buscar")
    public ResponseEntity<?> buscar(
            @RequestParam String criterio,
            @RequestParam(required = false, defaultValue = "") String valor) {
        try {
            return ResponseEntity.ok(mapper.toDtoList(servicio.buscar(criterio, valor)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "No se pudo realizar la búsqueda."));
        }
    }

    // Libros del catálogo que no están asignados a ninguna persona
    @GetMapping("/disponibles")
    public ResponseEntity<List<LibroDTO>> listarDisponibles() {
        return ResponseEntity.ok(mapper.toDtoList(servicio.listarDisponibles()));
    }

    // Alta de libros: el tipo (FISICO o DIGITAL) va en la URL y la fábrica correspondiente arma el libro
    @PostMapping("/{tipo}")
    public ResponseEntity<?> crear(
            @PathVariable String tipo,
            @RequestBody LibroDTO dto) {
        try {
            // DTO -> datos de alta (adaptador) -> fábrica del tipo -> entidad -> DTO de respuesta (adaptador)
            LibroSolicitud solicitud = libroMapper.toSolicitud(dto);
            Libro libro = servicio.crearLibro(TipoLibro.desde(tipo), solicitud);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(libro));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "No se pudo crear el libro."));
        }
    }

    // El alta genérica queda deshabilitada: un libro siempre se crea indicando su tipo
    @Override
    @PostMapping("")
    public ResponseEntity<?> save(@RequestBody LibroDTO dto) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(Map.of(
                "error", "Los libros se crean indicando el tipo: POST /api/v1/libros/FISICO o /api/v1/libros/DIGITAL."));
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