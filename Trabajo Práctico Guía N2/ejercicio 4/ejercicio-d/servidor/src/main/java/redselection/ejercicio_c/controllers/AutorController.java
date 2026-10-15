package redselection.ejercicio_c.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redselection.ejercicio_c.dto.AutorDTO;
import redselection.ejercicio_c.entities.Autor;
import redselection.ejercicio_c.services.AutorServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api/v1/autores")
public class AutorController extends BaseControllerImpl<Autor, AutorDTO, AutorServiceImpl> {

    @PostMapping("/{id}/clonar")
    public ResponseEntity<?> clonar(@PathVariable Long id) {
        try {
            Autor copia = servicio.clonar(id);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(copia));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"No se pudo clonar el autor.\"}");
        }
    }
}