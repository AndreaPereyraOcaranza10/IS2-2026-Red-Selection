package com.tienda.zero.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Un movimiento de stock. Cada línea de factura genera uno; anular una factura
 * genera otro compensatorio, así el historial nunca se borra.
 */
@Entity
@Table(name = "stock")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** Cambio de este movimiento: positivo si ingresa mercadería, negativo si sale. */
    @Column(nullable = false)
    private int movimiento;

    /** Saldo del producto después de este movimiento (el cantidadActual del UML). */
    @Column(nullable = false)
    private int cantidadActual;

    @Column
    private String observacion;

    /** No está en el UML: permite mostrar el historial en orden. */
    @Column(nullable = false)
    private LocalDateTime fecha;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "detalle_factura_id")
    private DetalleFactura detalleFactura;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "detalle_compra_id")
    private DetalleCompra detalleCompra;
}
