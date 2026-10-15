package redselection.clienteJava.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoriaClinicaDTO {
    private String id;
    private PacienteDTO paciente;
    private UsuarioDTO usuario;
    private List<DetalleHistoriaClinicaDTO> detalles = new ArrayList<>();
}
