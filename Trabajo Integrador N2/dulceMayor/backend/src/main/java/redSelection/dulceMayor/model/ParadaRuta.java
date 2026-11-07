package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import redSelection.dulceMayor.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "parada_ruta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ParadaRuta extends Base {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "ruta_id", nullable = false)
    private RutaDiaria ruta;

    @Column(nullable = false)
    private int orden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteMinorista cliente;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "pedido_id")
    private Pedido pedido;                         // en reparto

    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default
    private EstadoParada estado = EstadoParada.PENDIENTE;

    private LocalDateTime fechaHora;
}