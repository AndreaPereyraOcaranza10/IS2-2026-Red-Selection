package redselection.ejercicio_c.controllers;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.services.LibroServiceImpl;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api/v1/libros")
public class LibroController extends BaseControllerImpl<Libro, LibroServiceImpl> {

    @PostMapping(path = "/{id}/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirPdf(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.guardarPdf(id, archivo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> verPdf(@PathVariable Long id) {
        try {
            Resource pdf = servicio.obtenerPdf(id);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}