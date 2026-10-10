package redselection.cliente.util;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

public final class MensajesError {

    private MensajesError() {
    }

    public static String de(RestClientException e) {
        if (e instanceof ResourceAccessException) {
            return "No se pudo conectar con el servidor. Verificá que esté levantado.";
        }
        if (e instanceof RestClientResponseException r) {
            return "El servidor respondió " + r.getStatusCode().value() + ": " + r.getResponseBodyAsString();
        }
        return e.getMessage();
    }
}
