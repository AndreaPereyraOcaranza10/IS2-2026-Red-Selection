package redselection.ejercicio_c.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

// Producto concreto: libro en formato electrónico
@Entity
@DiscriminatorValue("DIGITAL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Audited
public class LibroDigital extends Libro {

    @Column(name = "formato", length = 10)
    private String formato;

    @Column(name = "tamanio_mb")
    private Double tamanioMb;

    @Column(name = "url_descarga", length = 500)
    private String urlDescarga;

    @Override
    public String getTipo() {
        return "DIGITAL";
    }
}