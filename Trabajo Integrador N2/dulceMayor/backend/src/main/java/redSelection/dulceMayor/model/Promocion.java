package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import redSelection.dulceMayor.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "promocion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Promocion extends Base {

    @Column(nullable = false, unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TipoPromocion tipoPromocion;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento;

    private Integer cantidadMinima;                       // solo VOLUMEN

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "producto_id")
    private Producto producto;                            // solo PRODUCTO_DESTACADO

    @Column(nullable = false)
    private LocalDate fechaDesde;

    private LocalDate fechaHasta;

    @Column(nullable = false) @Builder.Default
    private boolean activa = true;
}