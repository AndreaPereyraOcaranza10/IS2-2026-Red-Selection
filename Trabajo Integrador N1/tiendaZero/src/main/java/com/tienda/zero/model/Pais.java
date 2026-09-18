package com.tienda.zero.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pais {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private boolean eliminado;

}
