package com.tienda.zero.model;

import com.tienda.zero.enums.TipoDocumento;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;

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
public class Persona {

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

    // Relación opcional con la foto de perfil de la persona
    @ManyToOne
    @JoinColumn(name = "imagen_id")
    private Imagen imagen;

}