package com.tienda.zero.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalle_factura")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private double subtotal;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
}
