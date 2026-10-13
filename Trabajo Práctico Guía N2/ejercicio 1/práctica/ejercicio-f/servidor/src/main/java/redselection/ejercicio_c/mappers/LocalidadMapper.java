package redselection.ejercicio_c.mappers;

import org.mapstruct.Mapper;
import redselection.ejercicio_c.dto.LocalidadDTO;
import redselection.ejercicio_c.entities.Localidad;

@Mapper(componentModel = "spring")
public interface LocalidadMapper extends BaseMapper<Localidad, LocalidadDTO> {
}
