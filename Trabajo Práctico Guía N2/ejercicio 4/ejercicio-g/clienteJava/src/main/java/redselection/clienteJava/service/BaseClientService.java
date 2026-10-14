package redselection.clienteJava.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public abstract class BaseClientService<T> {

    protected final RestTemplate restTemplate;
    protected final String url;
    private final Class<T> tipo;
    private final Class<T[]> tipoArray;

    protected BaseClientService(RestTemplate restTemplate, String baseUrl, String recurso,
                                Class<T> tipo, Class<T[]> tipoArray) {
        this.restTemplate = restTemplate;
        this.url = baseUrl + "/" + recurso;
        this.tipo = tipo;
        this.tipoArray = tipoArray;
    }

    public List<T> listar() {
        T[] resultado = restTemplate.getForObject(url, tipoArray);
        return resultado == null ? List.of() : Arrays.asList(resultado);
    }

    public T obtener(String id) {
        return restTemplate.getForObject(url + "/{id}", tipo, id);
    }

    public T crear(T dto) {
        return restTemplate.postForObject(url, dto, tipo);
    }

    public T actualizar(String id, T dto) {
        return restTemplate.exchange(url + "/{id}", HttpMethod.PUT, new HttpEntity<>(dto), tipo, id).getBody();
    }

    public void eliminar(String id) {
        restTemplate.delete(url + "/{id}", id);
    }
}