package redselection.ejercicio_c.patrones.adaptador;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import redselection.ejercicio_c.dto.LibroDTO;
import redselection.ejercicio_c.dto.LibroSolicitud;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.entities.LibroDigital;
import redselection.ejercicio_c.entities.LibroFisico;
import redselection.ejercicio_c.patrones.fabrica.TipoLibro;

// Libro es abstracto y tiene dos subclases, pero el DTO es uno solo (plano, con "tipo").
// Por eso los dos métodos principales se escriben a mano (default) y eligen la subclase;
// el trabajo campo por campo lo genera MapStruct en los métodos @Named.
@Mapper(config = MapperConfiguracion.class, uses = AutorMapper.class)
public interface LibroMapper extends EntityMapper<Libro, LibroDTO> {

    // Entidad -> DTO: según la subclase real del libro
    @Override
    default LibroDTO toDto(Libro libro) {
        if (libro == null) {
            return null;
        }
        if (libro instanceof LibroDigital digital) {
            return digitalADto(digital);
        }
        if (libro instanceof LibroFisico fisico) {
            return fisicoADto(fisico);
        }
        throw new IllegalArgumentException(
                "Tipo de libro no soportado: " + libro.getClass().getSimpleName());
    }

    // DTO -> Entidad: según el "tipo" del DTO (si no viene, se asume FISICO)
    @Override
    default Libro toEntity(LibroDTO dto) {
        if (dto == null) {
            return null;
        }
        boolean sinTipo = dto.getTipo() == null || dto.getTipo().isBlank();
        TipoLibro tipo = sinTipo ? TipoLibro.FISICO : TipoLibro.desde(dto.getTipo());
        return tipo == TipoLibro.DIGITAL ? dtoADigital(dto) : dtoAFisico(dto);
    }

    // DTO -> datos de alta: es lo que recibe la fábrica para crear un libro nuevo
    LibroSolicitud toSolicitud(LibroDTO dto);

    // Métodos generados por MapStruct. Van con @Named para que MapStruct no los elija solo
    // (devuelven subclases de Libro y se confundirían con toEntity).
    @Named("fisicoADto")
    LibroDTO fisicoADto(LibroFisico libro);

    @Named("digitalADto")
    LibroDTO digitalADto(LibroDigital libro);

    @Named("dtoAFisico")
    LibroFisico dtoAFisico(LibroDTO dto);

    @Named("dtoADigital")
    LibroDigital dtoADigital(LibroDTO dto);
}