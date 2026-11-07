package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import redSelection.dulceMayor.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "configuracion_correo",
        uniqueConstraints = @UniqueConstraint(columnNames = {"dulce_mayor_id", "tipo_correo"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ConfiguracionCorreoEmpresa extends Base {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "dulce_mayor_id", nullable = false)
    private DulceMayor dulceMayor;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TipoCorreo tipoCorreo;

    @Column(nullable = false)
    private String asunto;

    @Lob @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String cuerpoHtml; // con {{marcadores}}
}