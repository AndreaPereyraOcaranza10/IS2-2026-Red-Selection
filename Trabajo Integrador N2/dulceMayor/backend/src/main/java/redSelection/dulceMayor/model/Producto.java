package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import redSelection.dulceMayor.enums.Presentacion;

/*
  El stock es centralizado: se calcula con los movimientos de la tabla stock
  (no atributo del producto). El precio sale de VigenciaPrecio.
 */
@Entity
@Table(name = "producto", indexes = @Index(name = "idx_producto_nombre", columnList = "nombre"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Producto extends Base {

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    private String marca;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Presentacion presentacion;

    /* Cantidad de unidades que trae el bulto o el pack. */
    @Column(nullable = false)
    private int unidadesPorPresentacion;

    /* Por debajo de este valor el producto se muestra como "poco stock". */
    @Column(nullable = false)
    @Builder.Default
    private int stockMinimo = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean enOferta = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subcategoria_id", nullable = false)
    private SubCategoria subCategoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imagen_id")
    private Imagen imagen;
}