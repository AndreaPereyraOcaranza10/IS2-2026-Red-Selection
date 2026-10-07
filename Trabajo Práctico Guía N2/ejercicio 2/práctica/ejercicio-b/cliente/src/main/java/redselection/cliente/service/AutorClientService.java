package redselection.cliente.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redselection.cliente.dto.AutorDTO;

import java.util.Arrays;
import java.util.List;

@Service
public class AutorClientService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AutorClientService(RestTemplate restTemplate, @Value("${api.base-url}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = apiBaseUrl + "/autores";
    }

    public List<AutorDTO> listar() {
        AutorDTO[] respuesta = restTemplate.getForObject(baseUrl, AutorDTO[].class);
        return respuesta == null ? List.of() : Arrays.asList(respuesta);
    }

    public AutorDTO crear(AutorDTO autor) {
        return restTemplate.postForObject(baseUrl, autor, AutorDTO.class);
    }
}
