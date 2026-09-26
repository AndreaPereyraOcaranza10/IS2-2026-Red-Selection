package com.tienda.zero.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String razonSocial;

    @Column(nullable = false)
    private boolean eliminado;

    @OneToMany
    @JoinColumn(name = "proveedor_id")
    @Builder.Default
    private List<Contacto> contactos = new ArrayList<>();
}