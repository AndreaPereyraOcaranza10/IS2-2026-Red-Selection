package com.tienda.zero.config;

import com.tienda.zero.service.FacturaCorreoPendiente;
import com.tienda.zero.service.FacturaCorreoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
@Slf4j
public class FacturaCorreoListener {
    private final FacturaCorreoService correos;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviarAlConfirmar(FacturaCorreoPendiente evento) {
        intentarEnviar(evento.facturaId());
    }

    @Scheduled(fixedDelayString = "${app.facturas.mail.reintento-ms:60000}", initialDelayString = "${app.facturas.mail.reintento-ms:60000}")
    public void reintentarPendientes() {
        for (String facturaId : correos.pendientes()) intentarEnviar(facturaId);
    }

    private void intentarEnviar(String facturaId) {
        try {
            correos.enviar(facturaId);
        } catch (RuntimeException e) {
            log.warn("La factura {} queda pendiente de envio por correo", facturaId, e);
        }
    }
}
