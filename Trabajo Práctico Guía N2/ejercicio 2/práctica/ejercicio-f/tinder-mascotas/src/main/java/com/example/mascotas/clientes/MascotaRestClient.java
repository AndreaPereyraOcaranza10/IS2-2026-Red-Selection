package com.example.mascotas.clientes;

import com.example.mascotas.dto.MascotaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/** Cliente HTTP para consumir la API, configurable con mascotas.api.base-url. */
@Component
public class MascotaRestClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public MascotaRestClient(RestTemplate restTemplate,
                             @Value("${mascotas.api.base-url:http://localhost:8080}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public MascotaDTO obtener(String id) {
        return restTemplate.getForObject(baseUrl + "/api/mascotas/{id}", MascotaDTO.class, id);
    }

    public List<MascotaDTO> listarPorUsuario(String usuarioId) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/api/mascotas")
                .queryParam("usuarioId", usuarioId).toUriString();
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<MascotaDTO>>() {}).getBody();
    }

    public MascotaDTO crear(MascotaDTO mascota) {
        return restTemplate.postForObject(baseUrl + "/api/mascotas", mascota, MascotaDTO.class);
    }

    public void actualizar(String id, MascotaDTO mascota) {
        restTemplate.put(baseUrl + "/api/mascotas/{id}", mascota, id);
    }

    public void eliminar(String id, String usuarioId) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/api/mascotas/{id}")
                .queryParam("usuarioId", "{usuarioId}").buildAndExpand(id, usuarioId).toUriString();
        restTemplate.delete(url);
    }
}
