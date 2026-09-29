package com.tienda.zero.model;

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

    @ManyToOne
    @JoinColumn(name = "detalle_factura_id", nullable = false)
    private DetalleFactura detalleFactura;
}