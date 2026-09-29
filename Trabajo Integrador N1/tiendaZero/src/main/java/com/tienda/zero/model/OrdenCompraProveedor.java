package com.tienda.zero.model;

import com.tienda.zero.enums.EstadoRecepcionCompra;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Registro operativo adicional para el aprovisionamiento solicitado en la consigna. */
@Entity @Table(name = "orden_compra") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrdenCompraProveedor {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @ManyToOne(optional = false) @JoinColumn(name = "proveedor_id") private Proveedor proveedor;
    @ManyToOne(optional = false) @JoinColumn(name = "producto_id") private Producto producto;
    @Column(nullable = false) private int cantidad;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal precioCompra;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal total;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private EstadoRecepcionCompra estado = EstadoRecepcionCompra.PENDIENTE_RECEPCION;
    @Column(nullable = false) private LocalDateTime fechaCreacion;
    private LocalDateTime fechaRecepcion;
}
