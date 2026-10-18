package redselection.ejercicio_c.patrones.adaptador;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import redselection.ejercicio_c.dto.LocalidadDTO;
import redselection.ejercicio_c.entities.Localidad;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-16T11:50:56-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Microsoft)"
)
@Component
public class LocalidadMapperImpl implements LocalidadMapper {

    @Override
    public LocalidadDTO toDto(Localidad entidad) {
        if ( entidad == null ) {
            return null;
        }

        LocalidadDTO localidadDTO = new LocalidadDTO();

        localidadDTO.setId( entidad.getId() );
        localidadDTO.setDenominacion( entidad.getDenominacion() );

        return localidadDTO;
    }

    @Override
    public Localidad toEntity(LocalidadDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Localidad localidad = new Localidad();

        localidad.setId( dto.getId() );
        localidad.setDenominacion( dto.getDenominacion() );

        return localidad;
    }

    @Override
    public List<LocalidadDTO> toDtoList(List<Localidad> entidades) {
        if ( entidades == null ) {
            return null;
        }

        List<LocalidadDTO> list = new ArrayList<LocalidadDTO>( entidades.size() );
        for ( Localidad localidad : entidades ) {
            list.add( toDto( localidad ) );
        }

        return list;
    }

    @Override
    public List<Localidad> toEntityList(List<LocalidadDTO> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<Localidad> list = new ArrayList<Localidad>( dtos.size() );
        for ( LocalidadDTO localidadDTO : dtos ) {
            list.add( toEntity( localidadDTO ) );
        }

        return list;
    }
}
