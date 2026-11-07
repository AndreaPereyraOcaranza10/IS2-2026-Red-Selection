package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import redSelection.dulceMayor.enums.TipoMovimientoStock;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Stock extends Base {

    /* Cambio de este movimiento: positivo si entra mercadería, negativo si sale. */
    @Column(nullable = false)
    private int movimiento;

    /* Saldo del producto inmediatamente después de este movimiento. */
    @Column(nullable = false)
    private int cantidadActual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimientoStock tipoMovimiento;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    //agregar relacion con detallefactura

}