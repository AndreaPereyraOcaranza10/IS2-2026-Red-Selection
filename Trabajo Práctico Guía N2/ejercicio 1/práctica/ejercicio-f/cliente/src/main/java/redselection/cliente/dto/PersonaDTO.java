package redselection.cliente.dto;

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
public class PersonaDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private Integer dni;
    private DomicilioDTO domicilio = new DomicilioDTO();
    private List<LibroDTO> libros = new ArrayList<>();

    /** El servidor puede devolver domicilio/localidad en null; el formulario necesita objetos no nulos. */
    public void asegurarDomicilio() {
        if (domicilio == null) {
            domicilio = new DomicilioDTO();
        }
        if (domicilio.getLocalidad() == null) {
            domicilio.setLocalidad(new LocalidadDTO());
        }
    }
}
