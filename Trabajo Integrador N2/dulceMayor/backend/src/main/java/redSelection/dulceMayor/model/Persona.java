package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import redSelection.dulceMayor.enums.TipoDocumento;
import redSelection.dulceMayor.enums.Sexo;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;


/**
 * Empleado y ResponsableComercio son personas.
 * La persona tiene a lo sumo un usuario (relación 1 a 1 opcional).
 */
@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public abstract class Persona extends Base {

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(nullable = false)
    private String numeroDocumento;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    private LocalDate fechaNacimiento;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "direccion_id", unique = true)
    private Direccion direccion;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "persona_id")
    @Builder.Default
    private List<Contacto> contactos = new ArrayList<>();
}

