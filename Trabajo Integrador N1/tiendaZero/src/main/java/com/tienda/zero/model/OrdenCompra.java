package com.tienda.zero.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tienda.zero.enums.EstadoOrdenCompra;
import com.tienda.zero.enums.TipoPago;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Orden de compra del cliente: funciona como carrito persistido, según el UML. */
@Entity @Table(name = "orden_compra_cliente") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrdenCompra {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false, unique = true) private String identificadorCompra;
    @Column(nullable = false) private LocalDate fecha;
    @Column(nullable = false) @Builder.Default private boolean eliminado = false;
    @Column(nullable = false, precision = 12, scale = 2) @Builder.Default private BigDecimal total = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private EstadoOrdenCompra estadoOrdenCompra = EstadoOrdenCompra.PENDIENTE_COMPLETAR;
    @Column(nullable = false) private String direccionEntrega;
    @Enumerated(EnumType.STRING) @Column private TipoPago formaPago;

    /** Identificador de la preferencia creada en Mercado Pago mediante el SDK. */
    @Column(length = 100) private String mpPreferenceId;

    /** Identificador del pago informado por Mercado Pago. */
    @Column(length = 50) private Long mpPaymentId;

    /** Último estado del pago informado por Mercado Pago. */
    @Column(length = 50) private String mpPaymentStatus;
    @JsonIgnore @ManyToOne @JoinColumn(name = "usuario_propietario_id") private Usuario propietario;
    @JsonIgnore @ManyToOne @JoinColumn(name = "cliente_id", nullable = true) private Cliente cliente;
    @JsonIgnore @ManyToOne @JoinColumn(name = "empleado_id") private Empleado empleado;
    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true) @Builder.Default private List<DetalleCompra> detalles = new ArrayList<>();
}


