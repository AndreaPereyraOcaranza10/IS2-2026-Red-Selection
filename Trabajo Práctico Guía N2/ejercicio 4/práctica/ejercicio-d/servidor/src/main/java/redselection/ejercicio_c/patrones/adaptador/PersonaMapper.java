package redselection.ejercicio_c.patrones.adaptador;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import redselection.ejercicio_c.dto.PersonaDTO;
import redselection.ejercicio_c.entities.Persona;

// El domicilio lo adapta DomicilioMapper y los libros LibroMapper
@Mapper(config = MapperConfiguracion.class, uses = {DomicilioMapper.class, LibroMapper.class})
public interface PersonaMapper extends EntityMapper<Persona, PersonaDTO> {

    // Los libros de una persona no se modifican editando a la persona: se asignan y desasignan
    // con PersonaService.asignarLibro / desasignarLibro. Por eso al pasar de DTO a entidad se ignoran.
    @Override
    @Mapping(target = "libros", ignore = true)
    Persona toEntity(PersonaDTO dto);
}