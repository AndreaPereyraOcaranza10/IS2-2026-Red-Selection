package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import redSelection.dulceMayor.dto.*;
import redSelection.dulceMayor.security.ControlDePropiedad;
import redSelection.dulceMayor.security.UsuarioAutenticado;
import redSelection.dulceMayor.service.RutaService;

/**
 * Rutas de preventa y de reparto. No extiende BaseController: no es un CRUD, son operaciones del día.
 * Acceso: por rol (SecurityConfig) y por propiedad (ControlDePropiedad): paradas y rutas solo las opera su responsable.
 */
@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    private final RutaService rutaService;
    private final ControlDePropiedad propiedad;

    public RutaController(RutaService rutaService, ControlDePropiedad propiedad) {
        this.rutaService = rutaService;
        this.propiedad = propiedad;
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
    public ResponseEntity<ParadaRutaDTO> registrarVisita(@PathVariable String paradaId,
                                                         @AuthenticationPrincipal UsuarioAutenticado usuario) {
        propiedad.exigirParada(usuario, paradaId);
        return ResponseEntity.ok(rutaService.registrarVisita(paradaId));
    }

    @PostMapping("/paradas/{paradaId}/entrega")
    public ResponseEntity<EntregaRealizadaDTO> registrarEntrega(@PathVariable String paradaId,
                                                                @AuthenticationPrincipal UsuarioAutenticado usuario) {
        propiedad.exigirParada(usuario, paradaId);
        return ResponseEntity.ok(rutaService.registrarEntrega(paradaId));
    }

    @PutMapping("/{rutaId}/orden")
    public ResponseEntity<RutaDTO> reordenar(@PathVariable String rutaId, @Valid @RequestBody OrdenarRutaDTO dto,
                                             @AuthenticationPrincipal UsuarioAutenticado usuario) {
        propiedad.exigirRuta(usuario, rutaId);
        return ResponseEntity.ok(rutaService.reordenar(rutaId, dto));
    }

    @PostMapping("/{rutaId}/optimizar")
    public ResponseEntity<PlanRutaDTO> optimizar(@PathVariable String rutaId,
                                                 @RequestBody(required = false) OptimizarRutaDTO dto,
                                                 @AuthenticationPrincipal UsuarioAutenticado usuario) {
        propiedad.exigirRuta(usuario, rutaId);
        return ResponseEntity.ok(rutaService.optimizar(rutaId, dto));
    }
}