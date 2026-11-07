package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
fechaHasta null = vigente hasta nuevo aviso.
*/
@Entity
@Table(name = "vigencia_precio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class VigenciaPrecio extends Base {

    @Column(nullable = false)
    private LocalDate fechaDesde;

    private LocalDate fechaHasta;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
}
