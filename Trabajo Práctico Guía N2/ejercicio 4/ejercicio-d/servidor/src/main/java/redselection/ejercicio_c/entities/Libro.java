package redselection.ejercicio_c.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table( name = "libro")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Audited
public class Libro extends Base {

    @Column(name="titulo")
    private String titulo;

    @Column(name="fecha")
    private int fecha;

    @Column(name="genero")
    private String genero;

    @Column(name="paginas")
    private int paginas;

    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    @ManyToMany(cascade = CascadeType.REFRESH)
    private List<Autor> autores;
}
