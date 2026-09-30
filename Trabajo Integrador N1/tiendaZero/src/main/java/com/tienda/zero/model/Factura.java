package com.tienda.zero.model;

import com.tienda.zero.enums.EstadoFactura;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "factura")
// JOINED: una tabla para lo común y una por tipo (FacturaProveedor ahora, FacturaCliente más adelante)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_factura", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public abstract class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private long numeroFactura;

    @Column(nullable = false)
    private Date fechaFactura;

    @Column(nullable = false)
    private double totalPagado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoFactura estado;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;

    @ManyToOne
    @JoinColumn(name = "forma_de_pago_id", nullable = false)
    private FormaDePago formaDePago;

    // Composición: los detalles no existen sin su factura (por eso cascade), pero sin
    // orphanRemoval, para respetar la baja lógica del proyecto.
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "factura_id")
    @Builder.Default
    private List<DetalleFactura> detalles = new ArrayList<>();

    /**
     * Cómo mueve el stock esta factura: +1 si lo suma (compra al proveedor),
     * -1 si lo resta (venta al cliente). Cada tipo de factura lo define, así
     * StockService nunca pregunta de qué tipo es (polimorfismo).
     */
    public abstract int signoStock();
}