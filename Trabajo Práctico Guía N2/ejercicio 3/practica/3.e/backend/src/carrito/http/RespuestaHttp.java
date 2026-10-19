package carrito.http;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class RespuestaHttp {
    public static void json(HttpExchange exchange, int status, String json) throws IOException {
        texto(exchange, status, json, "application/json; charset=utf-8");
    }

    public static void texto(HttpExchange exchange, int status, String contenido, String contentType)
            throws IOException {
        byte[] bytes = contenido.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

}
