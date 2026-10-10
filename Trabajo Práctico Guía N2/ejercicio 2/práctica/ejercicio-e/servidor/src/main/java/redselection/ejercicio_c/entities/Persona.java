package redselection.ejercicio_c.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table( name = "persona")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Audited
public class Persona extends Base {

    @Column(name ="nombre")
    private String nombre;

    @Column(name ="apellido")
    private String apellido;

    @Column(name ="dni")
    private int dni;

    @Column(name = "email")
    private String email;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "fk_domicilio")
    private Domicilio domicilio;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            name = "persona_libro",
            joinColumns = @JoinColumn(name = "persona_id"),
            inverseJoinColumns = @JoinColumn(name = "libro_id")
    )
    private List<Libro> libros = new ArrayList<Libro>();
}
