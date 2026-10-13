package redselection.ejercicio_c.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import redselection.ejercicio_c.patrones.prototipo.PrototipoAutor;

@Entity
@Table(name ="autor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class Autor extends Base implements PrototipoAutor<Autor> {

    @Column(name ="nombre")
    private String nombre;

    @Column(name ="apellido")
    private String apellido;

    @Column(name = "biografia", length = 1500)
    private String biografia;

    public Autor(Autor prototipo) {
        this.nombre = prototipo.getNombre();
        this.apellido = prototipo.getApellido();
        this.biografia = prototipo.getBiografia();
    }

    @Override
    public Autor clonar() {
        return new Autor(this);
    }

}
