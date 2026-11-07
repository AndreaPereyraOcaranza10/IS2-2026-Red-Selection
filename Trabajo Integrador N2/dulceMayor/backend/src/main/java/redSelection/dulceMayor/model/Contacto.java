package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import redSelection.dulceMayor.enums.TipoContacto;

@Entity
@Table(name = "contacto")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Contacto extends Base {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoContacto tipoContacto;

    private String observacion;

    /** El contacto principal es al que se envían la factura y los avisos. */
    @Column(nullable = false)
    @Builder.Default
    private boolean principal = false;
}
