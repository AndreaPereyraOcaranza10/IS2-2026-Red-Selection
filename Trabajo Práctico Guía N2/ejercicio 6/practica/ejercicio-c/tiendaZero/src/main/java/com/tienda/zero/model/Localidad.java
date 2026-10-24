package com.tienda.zero.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "localidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Localidad {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String codigoPostal;

    @Column(nullable = false)
    private boolean eliminado;

    @ManyToOne
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;
}
