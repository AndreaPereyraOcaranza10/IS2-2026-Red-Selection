package redselection.clienteJava.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redselection.clienteJava.dto.UsuarioDTO;

@Service
public class UsuarioClientService extends BaseClientService<UsuarioDTO> {
    public UsuarioClientService(RestTemplate restTemplate, @Value("${api.base-url}") String baseUrl) {
        super(restTemplate, baseUrl, "usuarios", UsuarioDTO.class, UsuarioDTO[].class);
    }
}