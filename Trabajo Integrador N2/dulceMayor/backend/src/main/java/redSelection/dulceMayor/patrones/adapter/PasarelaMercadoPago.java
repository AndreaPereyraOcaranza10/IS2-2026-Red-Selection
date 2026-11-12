package redSelection.dulceMayor.patrones.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import redSelection.dulceMayor.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * PATRÓN: Adapter (estructural). Traduce {@link PasarelaPago} a la Orders API de Mercado Pago (QR dinámico interoperable:
 * el mismo QR que muestra el posnet). Toda interacción con Mercado Pago pasa por su API REST; acá no hay SDK.
 * <ul>
 *   <li>Crear: {@code POST /v1/orders} (type=qr, mode=dynamic). El texto del QR viene en {@code type_response.qr_data}.</li>
 *   <li>Consultar: {@code GET /v1/orders/{id}} — es la ÚNICA fuente de verdad del estado del cobro.</li>
 *   <li>Cancelar: {@code POST /v1/orders/{id}/cancel} (solo órdenes en estado "created").</li>
 * </ul>
 * Credenciales solo por variables de entorno (MP_ACCESS_TOKEN, MP_POS_ID); nunca en el repositorio.
 * Se activa con {@code app.pagos.pasarela=mercadopago}.
 */
@Component
@ConditionalOnProperty(name = "app.pagos.pasarela", havingValue = "mercadopago")
public class PasarelaMercadoPago implements PasarelaPago {

    private static final Logger log = LoggerFactory.getLogger(PasarelaMercadoPago.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAPA = new ParameterizedTypeReference<>() { };

    private final RestClient client;
    private final String posExterno;
    private final String expiracion;

    public PasarelaMercadoPago(@Value("${app.mp.access-token:}") String accessToken,
                               @Value("${app.mp.pos-id:}") String posExterno,
                               @Value("${app.mp.base-url:https://api.mercadopago.com}") String baseUrl,
                               @Value("${app.mp.expiracion-qr:PT30M}") String expiracion) {
        if (accessToken.isBlank() || posExterno.isBlank()) {
            throw new IllegalStateException(
                    "Mercado Pago está activo pero faltan las variables de entorno MP_ACCESS_TOKEN y/o MP_POS_ID.");
        }
        this.posExterno = posExterno;
        this.expiracion = expiracion;
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();
        log.info("Pasarela de pago: Mercado Pago (Orders API), caja {}", posExterno);
    }

    @Override
    public OrdenPasarela crearOrden(String pagoId, BigDecimal monto, String descripcion) {
        String total = monto.setScale(2, RoundingMode.HALF_UP).toPlainString();
        Map<String, Object> cuerpo = Map.of(
                "type", "qr",
                "external_reference", "DM-" + pagoId,      // sin datos personales; <= 64 caracteres
                "description", recortar(descripcion, 150),
                "total_amount", total,
                "expiration_time", expiracion,
                "config", Map.of("qr", Map.of("external_pos_id", posExterno, "mode", "dynamic")),
                "transactions", Map.of("payments", List.of(Map.of("amount", total))),   // obligatorio en la Orders API
                "items", List.of(Map.of("title", recortar(descripcion, 150), "unit_price", total,
                        "quantity", 1, "unit_measure", "unit")));
        try {
            Map<String, Object> orden = client.post().uri("/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Idempotency-Key", pagoId)     // reintentar el mismo pago no duplica el cobro
                    .body(cuerpo)
                    .retrieve().body(MAPA);
            String id = texto(orden, "id");
            String qr = texto(mapa(orden, "type_response"), "qr_data");
            if (id == null || qr == null) {
                throw new ReglaNegocioException("Mercado Pago no devolvió el código QR. Intentá de nuevo.");
            }
            return new OrdenPasarela(id, qr);
        } catch (RestClientResponseException e) {
            log.error("Mercado Pago rechazó la creación de la orden ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ReglaNegocioException("No pudimos generar el cobro con Mercado Pago. Intentá de nuevo en unos minutos.");
        } catch (RuntimeException e) {
            if (e instanceof ReglaNegocioException) throw e;
            log.error("No se pudo comunicar con Mercado Pago al crear la orden: {}", e.getMessage());
            throw new ReglaNegocioException("No pudimos comunicarnos con Mercado Pago. Intentá de nuevo en unos minutos.");
        }
    }

    @Override
    public EstadoOrden consultar(String referencia) {
        try {
            Map<String, Object> orden = client.get().uri("/v1/orders/{id}", referencia).retrieve().body(MAPA);
            return aEstado(texto(orden, "status"), texto(orden, "status_detail"));
        } catch (RestClientResponseException e) {
            log.warn("Mercado Pago respondió {} al consultar la orden {}", e.getStatusCode(), referencia);
            throw new ReglaNegocioException("No pudimos consultar el estado del pago en Mercado Pago. Probá de nuevo.");
        } catch (RuntimeException e) {
            if (e instanceof ReglaNegocioException) throw e;
            log.warn("No se pudo consultar la orden {} en Mercado Pago: {}", referencia, e.getMessage());
            throw new ReglaNegocioException("No pudimos comunicarnos con Mercado Pago. Probá de nuevo.");
        }
    }

    @Override
    public void cancelar(String referencia) {
        try {
            client.post().uri("/v1/orders/{id}/cancel", referencia)
                    .header("X-Idempotency-Key", referencia + "-cancel")
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            HttpStatusCode estado = e.getStatusCode();
            if (estado.value() == 409) return;   // order_already_canceled: ya estaba cancelada
            throw e;                             // PagoService lo registra y sigue (best-effort)
        }
    }

    /**
     * Estado de la orden → estado del cobro. Solo "processed + accredited" cuenta como pagado.
     * Cancelada, vencida o reembolsada nunca se factura. Cualquier otro valor se trata como pendiente.
     */
    static EstadoOrden aEstado(String status, String statusDetail) {
        if (status == null) return EstadoOrden.PENDIENTE;
        return switch (status) {
            case "processed" -> "accredited".equals(statusDetail) || statusDetail == null
                    ? EstadoOrden.ACREDITADA : EstadoOrden.PENDIENTE;
            case "canceled", "expired", "refunded" -> EstadoOrden.CANCELADA;
            default -> EstadoOrden.PENDIENTE;
        };
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapa(Map<String, Object> origen, String clave) {
        Object valor = origen == null ? null : origen.get(clave);
        return valor instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
    }

    private static String texto(Map<String, Object> origen, String clave) {
        Object valor = origen == null ? null : origen.get(clave);
        return valor == null ? null : valor.toString();
    }

    private static String recortar(String texto, int max) {
        return texto == null ? "" : texto.length() <= max ? texto : texto.substring(0, max);
    }
}