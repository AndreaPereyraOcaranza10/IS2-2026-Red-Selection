package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import redSelection.dulceMayor.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Pedido extends Base {

    @Column(nullable = false)
    private LocalDateTime fechaHoraCarga;

    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default
    private EstadoPedido estado = EstadoPedido.CARGADO;

    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default
    private EstadoStockPedido estadoStock = EstadoStockPedido.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteMinorista cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "preventista_id", nullable = false)
    private Preventista preventista;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "promocion_id")
    private Promocion promocion;

    @Column(nullable = false, precision = 12, scale = 2) @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2) @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2) @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PedidoItem> items = new ArrayList<>();
}