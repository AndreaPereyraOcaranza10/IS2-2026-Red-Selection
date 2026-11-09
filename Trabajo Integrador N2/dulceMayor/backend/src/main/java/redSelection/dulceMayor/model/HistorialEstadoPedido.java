package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import redSelection.dulceMayor.enums.EstadoPedido;

import java.time.LocalDateTime;

/** Un cambio de estado de un pedido: cuándo pasó, quién lo hizo y por qué. Lo crea Pedido, nunca a mano. */
@Entity
@Table(name = "historial_estado_pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HistorialEstadoPedido extends Base {

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    /** Null en el primer registro (alta del pedido). */
    @Enumerated(EnumType.STRING)
    private EstadoPedido estadoAnterior;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private EstadoPedido estadoNuevo;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    /** Nombre de usuario de quien hizo el cambio, o "sistema" si fue automático. */
    @Column(nullable = false, length = 120)
    private String usuario;

    @Column(length = 500)
    private String observacion;
}
