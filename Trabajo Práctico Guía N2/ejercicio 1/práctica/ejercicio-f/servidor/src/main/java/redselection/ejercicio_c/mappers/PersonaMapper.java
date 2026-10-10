package redselection.ejercicio_c.mappers;

import org.mapstruct.Mapper;
import redselection.ejercicio_c.dto.PersonaDTO;
import redselection.ejercicio_c.entities.Persona;

@Mapper(componentModel = "spring", uses = {DomicilioMapper.class, LibroMapper.class})
public interface PersonaMapper extends BaseMapper<Persona, PersonaDTO> {
}
