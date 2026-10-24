package com.tienda.zero.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Departamento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private boolean eliminado;

    @ManyToOne
    @JoinColumn(name = "provincia_id", nullable = false)
    private Provincia provincia;
}
