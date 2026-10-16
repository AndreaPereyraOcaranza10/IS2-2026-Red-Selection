package redselection.ejercicio_c.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import redselection.ejercicio_c.entities.Autor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Datos que llegan para crear un libro. Todavía no es un Libro: la fábrica de cada tipo
// decide qué clase instanciar y qué campos usar.
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LibroSolicitud {

    // Datos comunes a todos los libros
    private String titulo;
    private LocalDate fechaPublicacion;
    private String genero;
    private Integer paginas;
    private List<Autor> autores = new ArrayList<>();

    // Solo libro físico
    private String ubicacion;

    // Solo libro digital
    private String formato;
    private Double tamanioMb;
    private String urlDescarga;
}