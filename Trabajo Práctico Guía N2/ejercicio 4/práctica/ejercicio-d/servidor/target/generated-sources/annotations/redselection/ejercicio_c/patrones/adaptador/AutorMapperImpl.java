package redselection.ejercicio_c.patrones.adaptador;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import redselection.ejercicio_c.dto.AutorDTO;
import redselection.ejercicio_c.entities.Autor;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-16T11:50:57-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Microsoft)"
)
@Component
public class AutorMapperImpl implements AutorMapper {

    @Override
    public AutorDTO toDto(Autor entidad) {
        if ( entidad == null ) {
            return null;
        }

        AutorDTO autorDTO = new AutorDTO();

        autorDTO.setId( entidad.getId() );
        autorDTO.setNombre( entidad.getNombre() );
        autorDTO.setApellido( entidad.getApellido() );
        autorDTO.setBiografia( entidad.getBiografia() );

        return autorDTO;
    }

    @Override
    public Autor toEntity(AutorDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Autor autor = new Autor();

        autor.setId( dto.getId() );
        autor.setNombre( dto.getNombre() );
        autor.setApellido( dto.getApellido() );
        autor.setBiografia( dto.getBiografia() );

        return autor;
    }

    @Override
    public List<AutorDTO> toDtoList(List<Autor> entidades) {
        if ( entidades == null ) {
            return null;
        }

        List<AutorDTO> list = new ArrayList<AutorDTO>( entidades.size() );
        for ( Autor autor : entidades ) {
            list.add( toDto( autor ) );
        }

        return list;
    }

    @Override
    public List<Autor> toEntityList(List<AutorDTO> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<Autor> list = new ArrayList<Autor>( dtos.size() );
        for ( AutorDTO autorDTO : dtos ) {
            list.add( toEntity( autorDTO ) );
        }

        return list;
    }
}
