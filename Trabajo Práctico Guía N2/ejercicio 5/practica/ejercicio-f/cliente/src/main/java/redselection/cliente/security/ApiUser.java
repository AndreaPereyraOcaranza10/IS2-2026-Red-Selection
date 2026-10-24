package redselection.cliente.security;

import org.springframework.security.core.AuthenticatedPrincipal;

import java.io.Serializable;

/**
 * Usuario logueado en la app web. Guarda el JWT que entregó la API para reenviarlo en cada llamada.
 * El token vive solo en la sesión del servidor del cliente: nunca se manda al navegador.
 */
public record ApiUser(String username, String token) implements AuthenticatedPrincipal, Serializable {

    @Override
    public String getName() {
        return username;
    }

    /** Evita que el token termine en logs. */
    @Override
    public String toString() {
        return "ApiUser[" + username + "]";
    }
}
