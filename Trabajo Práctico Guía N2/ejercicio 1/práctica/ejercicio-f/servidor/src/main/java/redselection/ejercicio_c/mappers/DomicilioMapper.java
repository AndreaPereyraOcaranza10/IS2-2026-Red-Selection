package redselection.ejercicio_c.mappers;

import org.mapstruct.Mapper;
import redselection.ejercicio_c.dto.DomicilioDTO;
import redselection.ejercicio_c.entities.Domicilio;

@Mapper(componentModel = "spring", uses = LocalidadMapper.class)
public interface DomicilioMapper extends BaseMapper<Domicilio, DomicilioDTO> {
}
