package redselection.ejercicio_c.mappers;

import org.mapstruct.Mapper;
import redselection.ejercicio_c.dto.AutorDTO;
import redselection.ejercicio_c.entities.Autor;

@Mapper(componentModel = "spring")
public interface AutorMapper extends BaseMapper<Autor, AutorDTO> {
}
