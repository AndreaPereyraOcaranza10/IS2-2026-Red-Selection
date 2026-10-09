package redselection.cliente.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import redselection.cliente.dto.LibroDTO;

import java.io.IOException;

@Service
public class LibroClientService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public LibroClientService(RestTemplate restTemplate, @Value("${api.base-url}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = apiBaseUrl + "/libros";
    }

    public LibroDTO subirPdf(Long libroId, MultipartFile pdf) throws IOException {
        ByteArrayResource contenido = new ByteArrayResource(pdf.getBytes()) {
            @Override
            public String getFilename() {
                return pdf.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> cuerpo = new LinkedMultiValueMap<>();
        cuerpo.add("archivo", contenido);

        HttpHeaders cabeceras = new HttpHeaders();
        cabeceras.setContentType(MediaType.MULTIPART_FORM_DATA);

        return restTemplate.postForObject(baseUrl + "/{id}/pdf", new HttpEntity<>(cuerpo, cabeceras),
                LibroDTO.class, libroId);
    }

    public byte[] obtenerPdf(Long libroId) {
        return restTemplate.getForObject(baseUrl + "/{id}/pdf", byte[].class, libroId);
    }
}