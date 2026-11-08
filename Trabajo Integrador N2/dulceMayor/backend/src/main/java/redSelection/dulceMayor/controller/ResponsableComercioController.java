package redSelection.dulceMayor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redSelection.dulceMayor.dto.ResponsableCrearDTO;
import redSelection.dulceMayor.dto.ResponsableRespuestaDTO;
import redSelection.dulceMayor.service.ResponsableComercioService;

import java.util.List;

/** El CRUD completo es del ADMINISTRATIVO; el preventista solo usa /buscar (ver SecurityConfig). */
@RestController
@RequestMapping("/api/responsables")
public class ResponsableComercioController extends BaseController<ResponsableCrearDTO, ResponsableRespuestaDTO> {

    private final ResponsableComercioService responsableService;

    public ResponsableComercioController(ResponsableComercioService responsableService) {
        super(responsableService);
        this.responsableService = responsableService;
    }

    @Override protected String idDe(ResponsableRespuestaDTO respuesta) { return respuesta.id(); }

    @GetMapping("/buscar")
    public ResponseEntity<List<ResponsableRespuestaDTO>> buscar(@RequestParam String q) {
        return ResponseEntity.ok(responsableService.buscar(q));
    }
}
