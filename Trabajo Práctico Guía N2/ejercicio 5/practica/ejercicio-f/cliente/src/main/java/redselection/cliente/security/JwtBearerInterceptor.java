package redselection.cliente.security;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

/** Agrega "Authorization: Bearer <token>" a TODAS las llamadas que el cliente le hace a la API. */
public class JwtBearerInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof ApiUser usuario) {
            request.getHeaders().setBearerAuth(usuario.token());
        }
        return execution.execute(request, body);
    }
}
