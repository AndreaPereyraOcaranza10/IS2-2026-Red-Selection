package redselection.ejercicio_c.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class BaseDTO implements Serializable {
    private Long id;
}
