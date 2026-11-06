package com.dulcemayor.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pedido_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PedidoItem {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(optional = false) @JoinColumn(name = "pedido_id") private Pedido pedido;
    @ManyToOne(optional = false) @JoinColumn(name = "producto_id") private Producto producto;
    @Column(nullable = false) private int cantidad;
    @Column(nullable = false) private BigDecimal precioUnitario;
    @Column(nullable = false) private BigDecimal total;
}
