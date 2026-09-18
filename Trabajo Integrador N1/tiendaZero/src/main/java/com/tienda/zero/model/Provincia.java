package com.tienda.zero.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "provincia", indexes = {
        @Index(name = "idx_provincia_nombre", columnList = "nombre")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Provincia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private boolean eliminado;

    @ManyToOne
    @JoinColumn(name = "pais_id", nullable = false)
    private Pais pais;
}









