package redselection.ejercicio_c.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PersonaDTO extends BaseDTO {
    private String nombre;
    private String apellido;
    private Integer dni;
    private DomicilioDTO domicilio;
    private List<LibroDTO> libros = new ArrayList<>();
}
