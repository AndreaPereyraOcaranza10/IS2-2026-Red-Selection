package redselection.ejercicio_c.patrones.adaptador;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redselection.ejercicio_c.dto.DomicilioDTO;
import redselection.ejercicio_c.entities.Domicilio;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-16T11:50:57-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Microsoft)"
)
@Component
public class DomicilioMapperImpl implements DomicilioMapper {

    @Autowired
    private LocalidadMapper localidadMapper;

    @Override
    public DomicilioDTO toDto(Domicilio entidad) {
        if ( entidad == null ) {
            return null;
        }

        DomicilioDTO domicilioDTO = new DomicilioDTO();

        domicilioDTO.setId( entidad.getId() );
        domicilioDTO.setCalle( entidad.getCalle() );
        domicilioDTO.setNumero( entidad.getNumero() );
        domicilioDTO.setLatitud( entidad.getLatitud() );
        domicilioDTO.setLongitud( entidad.getLongitud() );
        domicilioDTO.setLocalidad( localidadMapper.toDto( entidad.getLocalidad() ) );

        return domicilioDTO;
    }

    @Override
    public Domicilio toEntity(DomicilioDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Domicilio domicilio = new Domicilio();

        domicilio.setId( dto.getId() );
        domicilio.setCalle( dto.getCalle() );
        domicilio.setNumero( dto.getNumero() );
        domicilio.setLatitud( dto.getLatitud() );
        domicilio.setLongitud( dto.getLongitud() );
        domicilio.setLocalidad( localidadMapper.toEntity( dto.getLocalidad() ) );

        return domicilio;
    }

    @Override
    public List<DomicilioDTO> toDtoList(List<Domicilio> entidades) {
        if ( entidades == null ) {
            return null;
        }

        List<DomicilioDTO> list = new ArrayList<DomicilioDTO>( entidades.size() );
        for ( Domicilio domicilio : entidades ) {
            list.add( toDto( domicilio ) );
        }

        return list;
    }

    @Override
    public List<Domicilio> toEntityList(List<DomicilioDTO> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<Domicilio> list = new ArrayList<Domicilio>( dtos.size() );
        for ( DomicilioDTO domicilioDTO : dtos ) {
            list.add( toEntity( domicilioDTO ) );
        }

        return list;
    }
}
