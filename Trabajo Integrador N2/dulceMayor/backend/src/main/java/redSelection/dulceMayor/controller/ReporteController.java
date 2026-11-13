package redSelection.dulceMayor.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.dto.ArchivoReporteDTO;
import redSelection.dulceMayor.dto.ReporteDTO;
import redSelection.dulceMayor.service.ReporteService;

import java.time.LocalDate;

/**
 * Reportes (BK-10). Quién accede lo define SecurityConfig:
 *  - /api/reportes/{clientes|pedidos|envios|montos}[/pdf|/excel]  -> ADMINISTRATIVO (el Jefe lo hereda)
 *  - /api/reportes/globales/ventas[/pdf|/excel]                   -> solo JEFE_ADMINISTRATIVO
 * Sin extensión devuelve JSON (la pantalla); con /pdf o /excel, la descarga. Las fechas son opcionales
 * (yyyy-MM-dd); sin ellas, los últimos 30 días. "departamento" (nombre, opcional) filtra por el departamento del cliente.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/{tipo}")
    public ReporteDTO ver(@PathVariable String tipo,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                          @RequestParam(required = false) String departamento) {
        return reporteService.operativo(tipo, desde, hasta, departamento);
    }

    @GetMapping("/{tipo}/{formato}")
    public ResponseEntity<byte[]> descargar(@PathVariable String tipo, @PathVariable String formato,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                            @RequestParam(required = false) String departamento) {
        return archivo(reporteService.operativoExportado(tipo, formato, desde, hasta, departamento));
    }

    @GetMapping("/globales/ventas")
    public ReporteDTO ventas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                             @RequestParam(required = false) String departamento) {
        return reporteService.ventas(desde, hasta, departamento);
    }

    @GetMapping("/globales/ventas/{formato}")
    public ResponseEntity<byte[]> descargarVentas(@PathVariable String formato,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                                  @RequestParam(required = false) String departamento) {
        return archivo(reporteService.ventasExportado(formato, desde, hasta, departamento));
    }

    private ResponseEntity<byte[]> archivo(ArchivoReporteDTO reporte) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(reporte.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(reporte.nombreArchivo()).build().toString())
                .body(reporte.contenido());
    }
}