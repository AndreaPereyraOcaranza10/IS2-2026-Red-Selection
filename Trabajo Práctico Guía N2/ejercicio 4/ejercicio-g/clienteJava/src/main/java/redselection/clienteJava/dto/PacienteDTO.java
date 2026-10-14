package redselection.clienteJava.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PacienteDTO {
    private String id;
    private String nombre;
    private String apellido;
    private String documento;
    private FotoPacienteDTO fotoPaciente;
}
