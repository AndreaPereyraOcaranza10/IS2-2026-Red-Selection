package carrito.http;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ServidorEstatico {
    private static final Path PUBLIC_DIR = Path.of("frontend").toAbsolutePath().normalize();

    public static void servir(HttpExchange exchange, String requestPath) throws IOException {
        String path = "/".equals(requestPath) ? "/index.html" : requestPath;
        Path archivo = PUBLIC_DIR.resolve(URLDecoder.decode(path.substring(1), StandardCharsets.UTF_8)).normalize();

        if (!archivo.startsWith(PUBLIC_DIR) || !Files.exists(archivo) || Files.isDirectory(archivo)) {
            RespuestaHttp.texto(exchange, 404, "Archivo no encontrado", "text/plain; charset=utf-8");
            return;
        }

        RespuestaHttp.texto(exchange, 200, Files.readString(archivo), contentType(archivo));
    }

    private static String contentType(Path archivo) {
        String nombre = archivo.getFileName().toString();
        if (nombre.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (nombre.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (nombre.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        return "text/plain; charset=utf-8";
    }

}
