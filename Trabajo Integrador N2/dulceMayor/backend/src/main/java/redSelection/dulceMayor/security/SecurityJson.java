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
        String json = "{\"title\":\"" + titulo + "\",\"status\":" + status
                + ",\"detail\":\"" + detalle + "\",\"code\":\"" + codigo + "\"}";
        response.getWriter().write(json);
    }
}
