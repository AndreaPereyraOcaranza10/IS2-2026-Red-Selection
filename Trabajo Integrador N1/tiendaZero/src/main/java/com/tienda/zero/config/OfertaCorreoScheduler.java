package com.tienda.zero.config;

import com.tienda.zero.service.OfertaCorreoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OfertaCorreoScheduler {

    private final OfertaCorreoService ofertaCorreoService;

    public OfertaCorreoScheduler(OfertaCorreoService ofertaCorreoService) {
        this.ofertaCorreoService = ofertaCorreoService;
    }

    @Scheduled(fixedDelayString = "${app.ofertas.mail.intervalo-ms:864000000}",
            initialDelayString = "${app.ofertas.mail.initial-delay-ms:60000}")
    public void enviarOfertasCadaDiezDias() {
        ofertaCorreoService.enviarOfertasAClientesRegistrados();
    }
}
