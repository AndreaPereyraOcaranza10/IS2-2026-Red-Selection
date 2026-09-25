package com.tienda.zero.model;

import com.tienda.zero.enums.TipoContacto;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "contacto")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "forma_contacto", discriminatorType = DiscriminatorType.STRING)
public abstract class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoContacto tipoContacto;

    @Column
    private String observacion;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;
}
