package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import redSelection.dulceMayor.dto.DespachoCrearDTO;
import redSelection.dulceMayor.dto.DespachoRespuestaDTO;
import redSelection.dulceMayor.dto.TransportistaDespachoDTO;
import redSelection.dulceMayor.service.DespachoService;

import java.util.List;

/** Despacho de pedidos a un transportista. No extiende BaseController: no es un CRUD. Acceso: administración (SecurityConfig). */
@RestController
@RequestMapping("/api/despachos")
public class DespachoController {

    private final DespachoService despachoService;

    public DespachoController(DespachoService despachoService) {
        this.despachoService = despachoService;
    }

    @GetMapping("/transportistas")
    public ResponseEntity<List<TransportistaDespachoDTO>> transportistas() {
        return ResponseEntity.ok(despachoService.transportistasDisponibles());
    }

    @PostMapping
    public ResponseEntity<DespachoRespuestaDTO> asignar(@Valid @RequestBody DespachoCrearDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(despachoService.asignar(dto));
    }
}