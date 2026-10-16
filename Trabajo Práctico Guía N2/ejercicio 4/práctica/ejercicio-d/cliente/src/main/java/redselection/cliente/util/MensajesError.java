package redselection.cliente.util;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MensajesError {

    // Cuerpo de error del servidor: {"error":"mensaje"}
    private static final Pattern CAMPO_ERROR = Pattern.compile("\"error\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    private MensajesError() {
    }

    public static String de(RestClientException e) {
        if (e instanceof ResourceAccessException) {
            return "No se pudo conectar con el servidor. Verificá que esté levantado.";
        }
        if (e instanceof RestClientResponseException r) {
            String cuerpo = r.getResponseBodyAsString();
            Matcher m = CAMPO_ERROR.matcher(cuerpo);
            if (m.find()) {
                return m.group(1).replace("\\\"", "\"");
            }
            return "El servidor respondió " + r.getStatusCode().value() + ": " + cuerpo;
        }
        return e.getMessage();
    }
}
