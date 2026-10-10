package redselection.ejercicio_c.mappers;

import org.mapstruct.Mapper;
import redselection.ejercicio_c.dto.LibroDTO;
import redselection.ejercicio_c.entities.Libro;

@Mapper(componentModel = "spring", uses = AutorMapper.class)
public interface LibroMapper extends BaseMapper<Libro, LibroDTO> {
}
