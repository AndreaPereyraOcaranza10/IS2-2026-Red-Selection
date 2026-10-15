package redselection.ejercicio_c.patrones.adaptador;

import org.mapstruct.Mapper;
import redselection.ejercicio_c.dto.LocalidadDTO;
import redselection.ejercicio_c.entities.Localidad;

@Mapper(config = MapperConfiguracion.class)
public interface LocalidadMapper extends EntityMapper<Localidad, LocalidadDTO> {
}