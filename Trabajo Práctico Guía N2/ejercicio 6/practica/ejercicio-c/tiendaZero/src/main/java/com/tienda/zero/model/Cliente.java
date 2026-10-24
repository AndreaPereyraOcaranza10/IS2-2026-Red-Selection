package com.tienda.zero.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "cliente")
@DiscriminatorValue("CLIENTE")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Cliente extends Persona {

    @Column(nullable = false)
    private String direccionEstadia;

    @ManyToOne
    @JoinColumn(name = "nacionalidad_id", nullable = false)
    private Nacionalidad nacionalidad;
}