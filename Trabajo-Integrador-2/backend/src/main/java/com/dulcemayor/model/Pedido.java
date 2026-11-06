package com.dulcemayor.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false) private String cliente;
    @Column(nullable = false) @Builder.Default private String estado = "RECIBIDO";
    @Column(nullable = false) @Builder.Default private String medioPago = "A_COORDINAR";
    @Column(nullable = false) private BigDecimal total;
    @Column(nullable = false) private LocalDateTime fechaCarga;
    private LocalDateTime fechaFacturacion;
    private LocalDateTime fechaEntrega;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<PedidoItem> items = new ArrayList<>();
}
