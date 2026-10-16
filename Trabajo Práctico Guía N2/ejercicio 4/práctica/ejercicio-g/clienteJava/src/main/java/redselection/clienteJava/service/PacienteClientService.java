package redselection.clienteJava.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redselection.clienteJava.dto.PacienteDTO;

@Service
public class PacienteClientService extends BaseClientService<PacienteDTO> {
    public PacienteClientService(RestTemplate restTemplate, @Value("${api.base-url}") String baseUrl) {
        super(restTemplate, baseUrl, "pacientes", PacienteDTO.class, PacienteDTO[].class);
    }
}
