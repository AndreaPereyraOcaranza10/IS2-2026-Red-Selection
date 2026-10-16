package redselection.ejercicio_c.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.List;

// Producto abstracto del patrón Factory Method: ya no se instancia directamente,
// los libros se crean con una fábrica (ver patrones/fabrica).
// Los libros físicos y digitales comparten la tabla "libro"; la columna "tipo" indica cuál es cada fila.
@Entity
@Table( name = "libro")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "tipo",
        discriminatorType = DiscriminatorType.STRING,
        columnDefinition = "VARCHAR(10) NOT NULL DEFAULT 'FISICO'")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Audited
// En el JSON siempre viaja la propiedad "tipo" (FISICO / DIGITAL), que sale de getTipo(),
// y al leer un JSON se usa para saber qué subclase instanciar
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "tipo",
        defaultImpl = LibroFisico.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LibroFisico.class, name = "FISICO"),
        @JsonSubTypes.Type(value = LibroDigital.class, name = "DIGITAL")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Libro extends Base {

    @Column(name="titulo")
    private String titulo;

    @Column(name="fecha")
    private LocalDate fechaPublicacion;

    @Column(name="genero")
    private String genero;

    @Column(name="paginas")
    private int paginas;

    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    @ManyToMany(cascade = CascadeType.REFRESH)
    private List<Autor> autores;

    // Cada subclase informa su tipo. No es una columna: la columna "tipo" la maneja JPA como discriminador
    public abstract String getTipo();
}