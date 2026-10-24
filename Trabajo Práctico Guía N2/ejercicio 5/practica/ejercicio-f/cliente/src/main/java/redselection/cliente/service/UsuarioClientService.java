package redselection.cliente.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/** Registro de usuarios: llama al endpoint público POST /auth/register de la API (crea siempre rol USER). */
@Service
public class UsuarioClientService {

    // Sin el interceptor del token: el registro es público y todavía no hay sesión.
    private final RestTemplate restTemplate = new RestTemplate();
    private final String registerUrl;

    public UsuarioClientService(@Value("${api.base-url}") String apiBaseUrl) {
        this.registerUrl = apiBaseUrl + "/auth/register";
    }

    public void registrar(String username, String password) {
        restTemplate.postForObject(registerUrl, Map.of("username", username, "password", password), Map.class);
    }
}
