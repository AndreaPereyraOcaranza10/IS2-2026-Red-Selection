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

    // El tipo (FISICO / DIGITAL) va en la URL: el servidor elige la fábrica que corresponde
    public LibroDTO crear(LibroDTO libro) {
        return restTemplate.postForObject(
                baseUrl + "/{tipo}", libro, LibroDTO.class, libro.getTipo());
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

    // Búsqueda por TITULO, GENERO o AUTOR: el servidor elige la estrategia según el criterio
    public List<LibroDTO> buscar(String criterio, String valor) {
        LibroDTO[] respuesta = restTemplate.getForObject(
                baseUrl + "/buscar?criterio={criterio}&valor={valor}",
                LibroDTO[].class,
                criterio,
                valor == null ? "" : valor);

        return respuesta == null ? List.of() : Arrays.asList(respuesta);
    }

    /** Libros del catálogo que no están asignados a ninguna persona. */
    public List<LibroDTO> listarDisponibles() {
        LibroDTO[] respuesta = restTemplate.getForObject(
                baseUrl + "/disponibles", LibroDTO[].class);

        return respuesta == null ? List.of() : Arrays.asList(respuesta);
    }
}