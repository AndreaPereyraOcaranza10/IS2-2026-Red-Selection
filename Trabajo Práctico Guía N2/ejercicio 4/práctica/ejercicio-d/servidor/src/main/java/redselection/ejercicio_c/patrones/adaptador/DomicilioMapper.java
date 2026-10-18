package redselection.ejercicio_c.patrones.adaptador;

import org.mapstruct.Mapper;
import redselection.ejercicio_c.dto.DomicilioDTO;
import redselection.ejercicio_c.entities.Domicilio;

// "uses": para el campo localidad delega en LocalidadMapper
@Mapper(config = MapperConfiguracion.class, uses = LocalidadMapper.class)
public interface DomicilioMapper extends EntityMapper<Domicilio, DomicilioDTO> {
}