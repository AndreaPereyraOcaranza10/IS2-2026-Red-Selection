package redselection.ejercicio_c.controllers;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redselection.ejercicio_c.entities.Persona;
import redselection.ejercicio_c.services.PersonaServiceImpl;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api/v1/personas")
public class PersonaController extends BaseControllerImpl<Persona, PersonaServiceImpl>{
    @GetMapping("/search")
    public ResponseEntity<?> search (@RequestParam String filtro) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.search(filtro));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(("{\"error\":" + e.getMessage() + "}"));
        }
    }
}
