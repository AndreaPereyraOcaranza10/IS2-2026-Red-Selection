package redSelection.dulceMayor.controller;

import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.dto.NewsletterEnvioRespuestaDTO;
import redSelection.dulceMayor.dto.NewsletterProgramarDTO;
import redSelection.dulceMayor.dto.NewsletterProgramadaRespuestaDTO;
import redSelection.dulceMayor.service.NewsletterService;

/** Envío manual de demostración de BK-12. */
@RestController
@RequestMapping("/api/newsletter")
public class NewsletterController {

    private final NewsletterService newsletterService;

    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @PostMapping("/enviar")
    public ResponseEntity<NewsletterEnvioRespuestaDTO> enviar() {
        return ResponseEntity.ok(newsletterService.enviarAhora());
    }

    @PostMapping("/programar")
    public ResponseEntity<NewsletterProgramadaRespuestaDTO> programar(@Valid @RequestBody NewsletterProgramarDTO solicitud) {
        return ResponseEntity.accepted().body(new NewsletterProgramadaRespuestaDTO(
                solicitud.minutos(), newsletterService.programarUnaVez(solicitud.minutos())));
    }
}
