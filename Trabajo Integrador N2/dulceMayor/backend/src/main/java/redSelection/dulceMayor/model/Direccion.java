package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * La latitud y la longitud permiten ordenar la ruta diaria y mostrar el mapa.
 * Son opcionales en la entidad; se exigen en los puntos de venta (en el service).
 * Los datos territoriales se copian de la API Georef al elegirlos.
 * El país es siempre Argentina. Las coordenadas son opcionales; se exigen en los puntos de venta.
 */
@Entity
@Table(name = "direccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Direccion extends Base {

    @Column(nullable = false)
    private String calle;

    @Column(nullable = false)
    private String numeracion;

    private String barrio;
    private String referencia;
    private String manzanaPiso;
    private String casaDepartamento;
    private String codigoPostal;
    private Double latitud;
    private Double longitud;

    @Column(nullable = false)
    private String provinciaId;
    @Column(nullable = false)
    private String provinciaNombre;

    @Column(nullable = false)
    private String departamentoId;
    @Column(nullable = false)
    private String departamentoNombre;

    @Column(nullable = false)
    private String localidadId;
    @Column(nullable = false)
    private String localidadNombre;
}