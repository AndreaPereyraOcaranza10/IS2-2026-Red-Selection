package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import redSelection.dulceMayor.dto.PagoCrearDTO;
import redSelection.dulceMayor.dto.PagoRespuestaDTO;
import redSelection.dulceMayor.service.PagoService;

import java.net.URI;
import java.util.List;

/**
 * Pagos de una factura. No usa BaseController porque no es un CRUD: son acciones de un proceso
 * (iniciar, confirmar, anular). Los roles se declaran en SecurityConfig; el control de "es mi factura"
 * para el cliente lo hace PagoService.
 */
@RestController
@RequestMapping("/api")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) { this.pagoService = pagoService; }

    @PostMapping("/facturas/{facturaId}/pagos")
    public ResponseEntity<PagoRespuestaDTO> iniciar(@PathVariable String facturaId,
                                                    @Valid @RequestBody PagoCrearDTO dto) {
        PagoRespuestaDTO creado = pagoService.iniciar(facturaId, dto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/pagos/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @GetMapping("/facturas/{facturaId}/pagos")
    public List<PagoRespuestaDTO> listar(@PathVariable String facturaId) {
        return pagoService.listarPorFactura(facturaId);
    }

    @GetMapping("/pagos/{id}")
    public PagoRespuestaDTO obtener(@PathVariable String id) { return pagoService.obtener(id); }

    @PostMapping("/pagos/{id}/confirmar")
    public PagoRespuestaDTO confirmar(@PathVariable String id) { return pagoService.confirmar(id); }

    @PostMapping("/pagos/{id}/anular")
    public PagoRespuestaDTO anular(@PathVariable String id) { return pagoService.anular(id); }
}
