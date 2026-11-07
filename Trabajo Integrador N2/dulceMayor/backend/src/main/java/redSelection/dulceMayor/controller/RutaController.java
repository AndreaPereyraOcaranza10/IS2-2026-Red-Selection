package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redSelection.dulceMayor.dto.*;
import redSelection.dulceMayor.service.RutaService;

/**
 * Rutas de preventa y de reparto. No extiende BaseController: no es un CRUD, son operaciones del día.
 * El empleado se identifica por id en la URL; cuando se incorpore Spring Security (JWT) se tomará del usuario
 * autenticado y estas rutas quedarán restringidas por rol.
 */
@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    private final RutaService rutaService;

    public RutaController(RutaService rutaService) {
        this.rutaService = rutaService;
    }

    @GetMapping("/preventista/{preventistaId}/hoy")
    public ResponseEntity<RutaDTO> rutaDePreventista(@PathVariable String preventistaId) {
        return ResponseEntity.ok(rutaService.rutaDePreventista(preventistaId));
    }

    @GetMapping("/transportista/{transportistaId}/hoy")
    public ResponseEntity<RutaDTO> rutaDeTransportista(@PathVariable String transportistaId) {
        return ResponseEntity.ok(rutaService.rutaDeTransportista(transportistaId));
    }

    @PostMapping("/paradas/{paradaId}/visita")
    public ResponseEntity<ParadaRutaDTO> registrarVisita(@PathVariable String paradaId) {
        return ResponseEntity.ok(rutaService.registrarVisita(paradaId));
    }

    @PostMapping("/paradas/{paradaId}/entrega")
    public ResponseEntity<EntregaRealizadaDTO> registrarEntrega(@PathVariable String paradaId) {
        return ResponseEntity.ok(rutaService.registrarEntrega(paradaId));
    }

    @PutMapping("/{rutaId}/orden")
    public ResponseEntity<RutaDTO> reordenar(@PathVariable String rutaId, @Valid @RequestBody OrdenarRutaDTO dto) {
        return ResponseEntity.ok(rutaService.reordenar(rutaId, dto));
    }

    @PostMapping("/{rutaId}/optimizar")
    public ResponseEntity<PlanRutaDTO> optimizar(@PathVariable String rutaId,
                                             @RequestBody(required = false) OptimizarRutaDTO dto) {
        return ResponseEntity.ok(rutaService.optimizar(rutaId, dto));
    }
}
