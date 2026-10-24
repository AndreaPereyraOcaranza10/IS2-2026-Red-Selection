package redselection.cliente.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * El cliente no tiene base de usuarios propia: valida el usuario/contraseña llamando a POST /auth/login de la API.
 * Si la API responde con un token, el usuario queda autenticado en la sesión web.
 */
@Component
public class ApiAuthenticationProvider implements AuthenticationProvider {

    // RestTemplate propio, SIN el interceptor del token (todavía no hay token).
    private final RestTemplate restTemplate = new RestTemplate();
    private final String loginUrl;

    public ApiAuthenticationProvider(@Value("${api.base-url}") String apiBaseUrl) {
        this.loginUrl = apiBaseUrl + "/auth/login";
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> respuesta = restTemplate.postForObject(
                    loginUrl, Map.of("username", username, "password", password), Map.class);

            if (respuesta == null || respuesta.get("token") == null) {
                throw new AuthenticationServiceException("Respuesta de login inválida");
            }
            String token = String.valueOf(respuesta.get("token"));
            String rol = String.valueOf(respuesta.get("rol"));

            return UsernamePasswordAuthenticationToken.authenticated(
                    new ApiUser(username, token), null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));

        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest e) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        } catch (RestClientException e) {
            throw new AuthenticationServiceException("No se pudo conectar con el servidor", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
