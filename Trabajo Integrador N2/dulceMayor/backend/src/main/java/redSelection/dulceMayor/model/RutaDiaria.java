package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import redSelection.dulceMayor.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "ruta_diaria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RutaDiaria extends Base {
    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TipoRuta tipoRuta;

    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default
    private EstadoRuta estado = EstadoRuta.PLANIFICADA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "responsable_id", nullable = false)
    private Empleado responsable;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC") @Builder.Default
    private List<ParadaRuta> paradas = new ArrayList<>();
}