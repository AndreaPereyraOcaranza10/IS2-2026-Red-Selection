package redSelection.dulceMayor.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.security.UsuarioAutenticado;
import redSelection.dulceMayor.service.FacturaPdf;

/** Descarga de facturas (BK-05). Quién puede ver cada una lo decide FacturaClienteService. */
@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaPdf pdf;

    public FacturaController(FacturaPdf pdf) {
        this.pdf = pdf;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargar(@PathVariable String id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        byte[] contenido = pdf.generar(id, usuario);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename("factura-" + id + ".pdf").build().toString())
                .body(contenido);
    }
}
