package redselection.cliente.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LibroDTO {
    private Long id;
    private String titulo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaPublicacion;

    private String genero;
    private Integer paginas;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaDevolucion;

    private List<AutorDTO> autores = new ArrayList<>();

    @JsonSetter("fechaPublicacion")
    public void leerFechaPublicacion(String texto) {
        this.fechaPublicacion = (texto == null || texto.length() < 10)
                ? null
                : LocalDate.parse(texto.substring(0, 10));
    }
}