package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import redSelection.dulceMayor.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "factura_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FacturaCliente extends Base {

    @Column(nullable = false, unique = true)
    private Long numeroFactura;

    @Column(nullable = false)
    private LocalDateTime fechaFactura;

    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR) @Column(nullable = false, length = 20) @Builder.Default
    private EstadoFactura estado = EstadoFactura.EMITIDA;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2) @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true) @Builder.Default
    private List<DetalleFactura> detalles = new ArrayList<>();
}
