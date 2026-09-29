package com.tienda.zero.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "detalle_compra") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DetalleCompra {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @JsonIgnore @ManyToOne(optional = false) @JoinColumn(name = "orden_compra_id") private OrdenCompra ordenCompra;
    @ManyToOne(optional = false) @JoinColumn(name = "producto_id") private Producto producto;
    @Column(nullable = false) private int cantidad;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal subtotal;
    @Column(nullable = false) @Builder.Default private boolean eliminado = false;
    @JsonIgnore @OneToMany(mappedBy = "detalleCompra") @Builder.Default private List<Stock> movimientosStock = new ArrayList<>();
}
