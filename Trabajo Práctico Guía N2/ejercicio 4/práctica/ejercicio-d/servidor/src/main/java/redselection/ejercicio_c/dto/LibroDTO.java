package redselection.ejercicio_c.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Cómo viaja un libro por la API. Es plano: sirve tanto para libros físicos como digitales,
// y "tipo" (FISICO / DIGITAL) indica cuáles de los campos propios corresponden.
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LibroDTO {
    private Long id;
    private String tipo;
    private String titulo;
    private LocalDate fechaPublicacion;
    private String genero;
    private Integer paginas;
    private LocalDate fechaDevolucion;
    private List<AutorDTO> autores = new ArrayList<>();

    // Solo libro físico
    private String ubicacion;

    // Solo libro digital
    private String formato;
    private Double tamanioMb;
    private String urlDescarga;
}