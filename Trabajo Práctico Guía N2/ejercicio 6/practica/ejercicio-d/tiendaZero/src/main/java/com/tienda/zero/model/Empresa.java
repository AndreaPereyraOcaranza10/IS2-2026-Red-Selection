package com.tienda.zero.model;

import com.tienda.zero.enums.TipoEmpresa;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "empresa")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String razonSocial;

    @Column(nullable = false, unique = true)
    private String cuit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEmpresa tipoEmpresa;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;

    @ManyToOne
    @JoinColumn(name = "direccion_id", nullable = false)
    private Direccion direccion;

    @ManyToOne
    @JoinColumn(name = "contacto_id", nullable = false)
    private Contacto contacto;
}
