package redselection.ejercicio_c.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
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
    private int dni;
    private String email;
    private LocalDate fechaNacimiento;
    private DomicilioDTO domicilio;
    private List<LibroDTO> libros = new ArrayList<>();
}