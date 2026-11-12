package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import redSelection.dulceMayor.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Pago extends Base {

    /** El pago nace asociado al pedido: la factura recién se emite cuando el pago se acredita (o al despachar en efectivo). */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    /** Null hasta que se emite la factura del pedido. */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "factura_id")
    private FacturaCliente factura;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TipoPago tipoPago;

    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal monto;
    private LocalDateTime fechaPago;

    //private String alias; TRANSFERENCIA

    @Column(length = 1000)
    private String codigoQr; //BILLETERA_VIRTUAL
}