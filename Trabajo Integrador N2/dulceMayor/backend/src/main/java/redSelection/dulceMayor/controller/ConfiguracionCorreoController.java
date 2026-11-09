package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redSelection.dulceMayor.dto.ConfiguracionCorreoDTO;
import redSelection.dulceMayor.dto.ConfiguracionCorreoRespuestaDTO;
import redSelection.dulceMayor.enums.TipoCorreo;
import redSelection.dulceMayor.service.ConfiguracionCorreoService;

import java.util.List;

/**
 * Plantillas de los correos de la empresa. Acceso: ADMINISTRATIVO (el Jefe lo hereda), ver SecurityConfig.
 * No extiende BaseController: se opera por tipo de correo, no por id (ver ConfiguracionCorreoService).
 */
@RestController
@RequestMapping("/api/configuracion-correo")
public class ConfiguracionCorreoController {

    private final ConfiguracionCorreoService configuracionService;

    public ConfiguracionCorreoController(ConfiguracionCorreoService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping
    public ResponseEntity<List<ConfiguracionCorreoRespuestaDTO>> listar() {
        return ResponseEntity.ok(configuracionService.listar());
    }

    @GetMapping("/{tipo}")
    public ResponseEntity<ConfiguracionCorreoRespuestaDTO> obtener(@PathVariable TipoCorreo tipo) {
        return ResponseEntity.ok(configuracionService.obtener(tipo));
    }

    @PutMapping("/{tipo}")
    public ResponseEntity<ConfiguracionCorreoRespuestaDTO> guardar(@PathVariable TipoCorreo tipo,
                                                                   @Valid @RequestBody ConfiguracionCorreoDTO dto) {
        return ResponseEntity.ok(configuracionService.guardar(tipo, dto));
    }

    /** Vuelve al texto por defecto. */
    @DeleteMapping("/{tipo}")
    public ResponseEntity<ConfiguracionCorreoRespuestaDTO> restaurar(@PathVariable TipoCorreo tipo) {
        return ResponseEntity.ok(configuracionService.restaurar(tipo));
    }
}