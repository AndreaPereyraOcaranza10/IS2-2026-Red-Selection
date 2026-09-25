package com.tienda.zero.model;

import com.tienda.zero.enums.TipoDocumento;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "persona")
//JOINED --> una tabla por clase, persona + cliente + empleado con FK compartida
@Inheritance(strategy = InheritanceType.JOINED)
//Agrega la columna a la tabla persona con el tipo de persona que es
@DiscriminatorColumn(name = "tipo_persona", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public abstract class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @Column(nullable = false)
    private Date fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(nullable = false)
    private String numeroDocumento;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;

    @ManyToOne
    @JoinColumn(name = "imagen_id")
    private Imagen imagen;

    @OneToMany
    @JoinColumn(name = "persona_id")
    @Builder.Default
    private List<Contacto> contactos = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "persona_id")
    @Builder.Default
    private List<Direccion> direcciones = new ArrayList<>();

}