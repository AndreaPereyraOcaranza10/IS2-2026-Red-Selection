package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import redSelection.dulceMayor.enums.TipoEmpresa;

import java.util.ArrayList;
import java.util.List;

/** La empresa mayorista: una fila por casa central o sucursal (se muestran en el sitio institucional). */
@Entity @Table(name = "dulce_mayor")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class DulceMayor extends Empresa {

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEmpresa tipoEmpresa;

    /** Alias al que los clientes hacen las transferencias. */
    private String aliasTransferencia;

    /** Logo y fotos de la sucursal (TipoImagen LOGO y FOTO_SUCURSAL). */
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "dulce_mayor_id")
    @Builder.Default
    private List<Imagen> imagenes = new ArrayList<>();
}