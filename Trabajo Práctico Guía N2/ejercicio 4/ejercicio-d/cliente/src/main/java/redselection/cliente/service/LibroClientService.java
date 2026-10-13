package redselection.cliente.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redselection.cliente.dto.LibroDTO;

import java.util.Arrays;
import java.util.List;

@Service
public class LibroClientService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public LibroClientService(
            RestTemplate restTemplate,
            @Value("${api.base-url}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = apiBaseUrl + "/libros";
    }

    public List<LibroDTO> listar() {
        LibroDTO[] respuesta =
                restTemplate.getForObject(baseUrl, LibroDTO[].class);

        return respuesta == null ? List.of() : Arrays.asList(respuesta);
    }

    public LibroDTO obtener(Long id) {
        return restTemplate.getForObject(
                baseUrl + "/{id}", LibroDTO.class, id);
    }

    public LibroDTO crear(LibroDTO libro) {
        return restTemplate.postForObject(
                baseUrl, libro, LibroDTO.class);
    }

    public void actualizar(Long id, LibroDTO libro) {
        restTemplate.put(baseUrl + "/{id}", libro, id);
    }

    public void eliminar(Long id) {
        restTemplate.delete(baseUrl + "/{id}", id);
    }

    public List<LibroDTO> listarPorAutor(Long autorId) {
        LibroDTO[] respuesta = restTemplate.getForObject(
                baseUrl + "/autor/{autorId}",
                LibroDTO[].class,
                autorId
        );

        return respuesta == null
                ? List.of()
                : Arrays.asList(respuesta);
    }
}