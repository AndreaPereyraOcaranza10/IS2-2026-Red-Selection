package redselection.clienteJava.util;

import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MensajesError {

    private static final Pattern ERROR = Pattern.compile("\"error\"\\s*:\\s*\"(.*?)\"");

    private MensajesError() { }

    public static String de(Exception e) {
        if (e instanceof HttpStatusCodeException http) {
            Matcher m = ERROR.matcher(http.getResponseBodyAsString());
            return m.find() ? m.group(1) : "Error del servidor (" + http.getStatusCode().value() + ")";
        }
        if (e instanceof ResourceAccessException) {
            return "No se pudo conectar con el servidor. ¿Está encendido?";
        }
        return "Error inesperado: " + e.getMessage();
    }
}