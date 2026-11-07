package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** Persona a cargo de uno o más comercios. Recibe factura, avisos y newsletter en su correo principal. */
@Entity
@Table(name = "responsable_comercio")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ResponsableComercio extends Persona { }
