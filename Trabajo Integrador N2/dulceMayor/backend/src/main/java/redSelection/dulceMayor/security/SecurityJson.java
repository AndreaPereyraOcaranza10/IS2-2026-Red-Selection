package redSelection.dulceMayor.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Respuestas de error de la capa de seguridad con el mismo formato (ProblemDetail) que el resto de la API. */
final class SecurityJson {

    private SecurityJson() { }

    static void responder(HttpServletResponse response, int status, String titulo, String detalle, String codigo)
            throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/problem+json");
        String json = "{\"title\":\"" + escapar(titulo) + "\",\"status\":" + status
                + ",\"detail\":\"" + escapar(detalle) + "\",\"code\":\"" + escapar(codigo) + "\"}";
        response.getWriter().write(json);
    }

    /** Escapa lo que rompería un string JSON (comillas, barras, saltos de línea y caracteres de control). */
    static String escapar(String texto) {
        if (texto == null) return "";
        StringBuilder sb = new StringBuilder(texto.length() + 8);
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
