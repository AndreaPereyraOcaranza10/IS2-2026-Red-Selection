package redselection.clienteJava.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redselection.clienteJava.dto.MedicoDTO;

@Service
public class MedicoClientService extends BaseClientService<MedicoDTO> {
    public MedicoClientService(RestTemplate restTemplate, @Value("${api.base-url}") String baseUrl) {
        super(restTemplate, baseUrl, "medicos", MedicoDTO.class, MedicoDTO[].class);
    }
}
