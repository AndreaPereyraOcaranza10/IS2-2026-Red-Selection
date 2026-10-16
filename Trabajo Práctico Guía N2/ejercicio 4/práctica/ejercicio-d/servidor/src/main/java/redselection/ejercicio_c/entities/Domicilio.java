package redselection.ejercicio_c.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Table( name = "domicilio")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Audited
public class Domicilio extends Base {

    @Column(name="calle")
    private String calle;

    @Column(name="numero")
    private int numero;

    @Column(name="latitud")
    private String latitud;

    @Column(name="longitud")
    private String longitud;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_localidad")
    private Localidad localidad;
}