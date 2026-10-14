package redselection.clienteJava.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redselection.clienteJava.dto.HistoriaClinicaDTO;

import java.util.Arrays;
import java.util.List;

@Service
public class HistoriaClinicaClientService extends BaseClientService<HistoriaClinicaDTO> {

    public HistoriaClinicaClientService(RestTemplate restTemplate, @Value("${api.base-url}") String baseUrl) {
        super(restTemplate, baseUrl, "historias-clinicas", HistoriaClinicaDTO.class, HistoriaClinicaDTO[].class);
    }

    public List<HistoriaClinicaDTO> listarPorPaciente(String pacienteId) {
        HistoriaClinicaDTO[] resultado =
                restTemplate.getForObject(url + "/paciente/{id}", HistoriaClinicaDTO[].class, pacienteId);
        return resultado == null ? List.of() : Arrays.asList(resultado);
    }
}
