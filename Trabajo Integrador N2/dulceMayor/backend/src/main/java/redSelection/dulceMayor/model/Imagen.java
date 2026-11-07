package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import redSelection.dulceMayor.enums.TipoImagen;

@Entity
@Table(name = "imagen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Imagen extends Base {
    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String mime;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] contenido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoImagen tipoImagen;
}