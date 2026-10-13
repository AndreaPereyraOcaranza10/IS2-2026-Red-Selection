package redselection.ejercicio_c.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AutorDTO extends BaseDTO {
    private String nombre;
    private String apellido;
    private String biografia;
}
