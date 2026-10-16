package redselection.cliente.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redselection.cliente.dto.LocalidadDTO;

import java.util.Arrays;
import java.util.List;

@Service
public class LocalidadClientService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public LocalidadClientService(RestTemplate restTemplate, @Value("${api.base-url}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = apiBaseUrl + "/localidades";
    }

    public List<LocalidadDTO> listar() {
        LocalidadDTO[] respuesta = restTemplate.getForObject(baseUrl, LocalidadDTO[].class);
        return respuesta == null ? List.of() : Arrays.asList(respuesta);
    }

    public LocalidadDTO crear(LocalidadDTO localidad) {
        return restTemplate.postForObject(baseUrl, localidad, LocalidadDTO.class);
    }
}
