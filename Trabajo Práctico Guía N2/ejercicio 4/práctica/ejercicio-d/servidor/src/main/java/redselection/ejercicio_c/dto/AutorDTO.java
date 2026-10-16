package redselection.ejercicio_c.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Cómo viaja un autor por la API: la entidad Autor nunca sale del servidor
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutorDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String biografia;
}