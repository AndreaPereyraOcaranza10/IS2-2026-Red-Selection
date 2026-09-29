package com.tienda.zero.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/** Movimiento de stock. cantidad es el delta firmado y cantidadActual el saldo tras el movimiento. */
@Entity @Table(name = "stock") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Stock {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private int cantidad;
    @Column(nullable = false) private int cantidadActual;
    @Column(nullable = false) @Builder.Default private boolean eliminado = false;
    private String observacion;
    @Column(nullable = false) private LocalDateTime fechaMovimiento;
    @JsonIgnore @ManyToOne(optional = false) @JoinColumn(name = "producto_id") private Producto producto;
    @ManyToOne @JoinColumn(name = "orden_compra_proveedor_id") private OrdenCompraProveedor ordenCompraProveedor;
    @JsonIgnore @ManyToOne @JoinColumn(name = "detalle_compra_id") private DetalleCompra detalleCompra;
}
