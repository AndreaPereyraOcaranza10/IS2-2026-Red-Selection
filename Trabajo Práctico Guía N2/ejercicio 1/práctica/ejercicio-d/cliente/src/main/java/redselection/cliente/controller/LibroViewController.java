package redselection.cliente.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClientException;
import redselection.cliente.service.LibroClientService;
import redselection.cliente.util.MensajesError;

@Controller
@RequestMapping("/libros")
public class LibroViewController {

    private final LibroClientService libroService;

    public LibroViewController(LibroClientService libroService) {
        this.libroService = libroService;
    }

    @GetMapping("/{libroId}/pdf")
    public ResponseEntity<?> verPdf(@PathVariable Long libroId) {
        try {
            byte[] pdf = libroService.obtenerPdf(libroId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(pdf);
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.parseMediaType("text/plain;charset=UTF-8"))
                    .body(MensajesError.de(e));
        }
    }
}