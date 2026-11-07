package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** Administrativo/Almacenero y Jefe. Los distingue Usuario.rol. */
@Entity
@Table(name = "empleado")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Empleado extends Persona {
}