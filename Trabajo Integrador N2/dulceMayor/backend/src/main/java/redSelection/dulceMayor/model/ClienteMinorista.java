package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** Punto de venta (kiosco, almacén, etc.). */
@Entity
@Table(name = "cliente_minorista")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ClienteMinorista extends Empresa {

    @Column(nullable = false)
    private String nombreComercio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsable_id", nullable = false)
    private ResponsableComercio responsable;

    /** Preventista que lo dio de alta y lo atiende en su ruta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preventista_id")
    private Preventista preventistaAsignado;
}
