package redselection.ejercicio_c.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

// Producto concreto: libro en papel, que se guarda en un lugar físico
@Entity
@DiscriminatorValue("FISICO")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Audited
public class LibroFisico extends Libro {

    @Column(name = "ubicacion")
    private String ubicacion;

    @Override
    public String getTipo() {
        return "FISICO";
    }
}