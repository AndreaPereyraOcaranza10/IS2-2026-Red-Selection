package redselection.ejercicio_c.patrones.adaptador;

import org.mapstruct.Mapper;
import redselection.ejercicio_c.dto.AutorDTO;
import redselection.ejercicio_c.entities.Autor;

@Mapper(config = MapperConfiguracion.class)
public interface AutorMapper extends EntityMapper<Autor, AutorDTO> {
}