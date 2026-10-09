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
public class LibroDTO {
    private Long id;
    private String titulo;
    private Integer fecha;
    private String genero;
    private Integer paginas;
    private List<AutorDTO> autores = new ArrayList<>();
    private String archivoPdf;
}
