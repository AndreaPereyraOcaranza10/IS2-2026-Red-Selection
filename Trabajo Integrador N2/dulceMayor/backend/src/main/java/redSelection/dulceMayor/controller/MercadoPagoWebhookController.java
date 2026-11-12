package redSelection.dulceMayor.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redSelection.dulceMayor.patrones.adapter.MercadoPagoFirma;
import redSelection.dulceMayor.service.PagoService;

import java.util.Map;

/**
 * Webhook de Mercado Pago (tópico "orders"). Es el ÚNICO endpoint público de pagos.
 * No se confía en el cuerpo: solo se toma el id de la orden y {@link PagoService#verificarPorReferencia} vuelve a
 * consultar su estado a la API de Mercado Pago. Con MP_WEBHOOK_SECRET configurado, además se valida la firma.
 * Responde 200 siempre que la notificación sea auténtica, para que Mercado Pago no reintente en loop.
 */
@RestController
@RequestMapping("/api/pagos/webhook")
@ConditionalOnProperty(name = "app.pagos.pasarela", havingValue = "mercadopago")
public class MercadoPagoWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoWebhookController.class);

    private final PagoService pagos;
    private final String secreto;

    public MercadoPagoWebhookController(PagoService pagos, @Value("${app.mp.webhook-secret:}") String secreto) {
        this.pagos = pagos;
        this.secreto = secreto;
    }

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> recibir(@RequestParam(name = "data.id", required = false) String dataIdQuery,
                                        @RequestHeader(name = "x-signature", required = false) String firma,
                                        @RequestHeader(name = "x-request-id", required = false) String requestId,
                                        @RequestBody(required = false) Map<String, Object> cuerpo) {
        String ordenId = dataIdQuery != null ? dataIdQuery : idDelCuerpo(cuerpo);
        if (ordenId == null || ordenId.isBlank()) {
            return ResponseEntity.ok().build();                       // nada que verificar (p. ej. ping de prueba)
        }
        if (!secreto.isBlank() && !MercadoPagoFirma.esValida(firma, requestId, ordenId, secreto)) {
            log.warn("Webhook de Mercado Pago con firma inválida (orden {})", ordenId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            pagos.verificarPorReferencia(ordenId);
        } catch (RuntimeException e) {
            // No se devuelve el error: el cliente igual puede verificar desde su portal y MP reintenta si no responde.
            log.warn("No se pudo procesar la notificación de la orden {}: {}", ordenId, e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unchecked")
    private static String idDelCuerpo(Map<String, Object> cuerpo) {
        if (cuerpo == null || !(cuerpo.get("data") instanceof Map<?, ?> data)) return null;
        Object id = ((Map<String, Object>) data).get("id");
        return id == null ? null : id.toString();
    }
}
