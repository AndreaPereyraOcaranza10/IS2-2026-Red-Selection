package redselection.ejercicio_c.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class LibroDTO extends BaseDTO {
    private String titulo;
    private Integer fecha;
    private String genero;
    private Integer paginas;
    private List<AutorDTO> autores = new ArrayList<>();
}
