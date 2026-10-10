package redselection.ejercicio_c.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DomicilioDTO extends BaseDTO {
    private String calle;
    private Integer numero;
    private LocalidadDTO localidad;
}
