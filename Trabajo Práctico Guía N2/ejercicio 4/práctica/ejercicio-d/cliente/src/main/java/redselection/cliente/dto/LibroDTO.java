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

    // FISICO o DIGITAL: lo define el servidor al crear el libro y no se puede cambiar después
    private String tipo = "FISICO";

    // Solo libro físico
    private String ubicacion;

    // Solo libro digital
    private String formato;
    private Double tamanioMb;
    private String urlDescarga;

    @JsonSetter("fechaPublicacion")
    public void leerFechaPublicacion(String texto) {
        this.fechaPublicacion = (texto == null || texto.length() < 10)
                ? null
                : LocalDate.parse(texto.substring(0, 10));
    }

    // Para las vistas (no lleva "get" ni "is" para que no se serialice como propiedad)
    public boolean esDigital() {
        return "DIGITAL".equals(tipo);
    }

    // Texto para elegir un libro en un combo: distingue libros con el mismo título
    // (tipo con su ubicación o formato, autores y año de publicación)
    public String etiqueta() {
        StringBuilder texto = new StringBuilder(titulo == null ? "" : titulo);
        texto.append(" — ");

        if (esDigital()) {
            texto.append("Digital");
            if (formato != null && !formato.isBlank()) {
                texto.append(" ").append(formato);
            }
        } else {
            texto.append("Físico");
            if (ubicacion != null && !ubicacion.isBlank()) {
                texto.append(" ").append(ubicacion);
            }
        }

        if (autores != null && !autores.isEmpty()) {
            texto.append(" · ");
            for (int i = 0; i < autores.size(); i++) {
                if (i > 0) {
                    texto.append(", ");
                }
                texto.append(autores.get(i).getNombre()).append(" ").append(autores.get(i).getApellido());
            }
        }

        if (fechaPublicacion != null) {
            texto.append(" · ").append(fechaPublicacion.getYear());
        }

        return texto.toString();
    }
}