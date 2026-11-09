package redSelection.dulceMayor.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redSelection.dulceMayor.dto.EntregaDetalleDTO;
import redSelection.dulceMayor.dto.EntregaResumenDTO;
import redSelection.dulceMayor.dto.PagoRespuestaDTO;
import redSelection.dulceMayor.service.EntregaService;

import java.time.LocalDate;
import java.util.List;

/**
 * Entregas del transportista: historial, detalle y cobro en efectivo. No extiende BaseController: no es un CRUD.
 * Acceso: por rol y por pertenencia en SecurityConfig (historial) y en EntregaService (detalle y cobro).
 */
@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @GetMapping("/transportista/{transportistaId}")
    public ResponseEntity<List<EntregaResumenDTO>> historial(
            @PathVariable String transportistaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(entregaService.historial(transportistaId, desde, hasta));
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<EntregaDetalleDTO> detalle(@PathVariable String pedidoId) {
        return ResponseEntity.ok(entregaService.detalle(pedidoId));
    }

    @PostMapping("/pedido/{pedidoId}/cobro")
    public ResponseEntity<PagoRespuestaDTO> cobrarEnEfectivo(@PathVariable String pedidoId) {
        return ResponseEntity.ok(entregaService.cobrarEnEfectivo(pedidoId));
    }
}
