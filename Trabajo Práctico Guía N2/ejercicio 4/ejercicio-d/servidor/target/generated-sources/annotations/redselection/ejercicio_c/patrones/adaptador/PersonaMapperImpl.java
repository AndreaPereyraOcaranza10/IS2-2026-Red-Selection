package redselection.ejercicio_c.patrones.adaptador;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redselection.ejercicio_c.dto.PersonaDTO;
import redselection.ejercicio_c.entities.Persona;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-15T11:35:11-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Microsoft)"
)
@Component
public class PersonaMapperImpl implements PersonaMapper {

    @Autowired
    private DomicilioMapper domicilioMapper;
    @Autowired
    private LibroMapper libroMapper;

    @Override
    public PersonaDTO toDto(Persona entidad) {
        if ( entidad == null ) {
            return null;
        }

        PersonaDTO personaDTO = new PersonaDTO();

        personaDTO.setId( entidad.getId() );
        personaDTO.setNombre( entidad.getNombre() );
        personaDTO.setApellido( entidad.getApellido() );
        personaDTO.setDni( entidad.getDni() );
        personaDTO.setEmail( entidad.getEmail() );
        personaDTO.setFechaNacimiento( entidad.getFechaNacimiento() );
        personaDTO.setDomicilio( domicilioMapper.toDto( entidad.getDomicilio() ) );
        personaDTO.setLibros( libroMapper.toDtoList( entidad.getLibros() ) );

        return personaDTO;
    }

    @Override
    public List<PersonaDTO> toDtoList(List<Persona> entidades) {
        if ( entidades == null ) {
            return null;
        }

        List<PersonaDTO> list = new ArrayList<PersonaDTO>( entidades.size() );
        for ( Persona persona : entidades ) {
            list.add( toDto( persona ) );
        }

        return list;
    }

    @Override
    public List<Persona> toEntityList(List<PersonaDTO> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<Persona> list = new ArrayList<Persona>( dtos.size() );
        for ( PersonaDTO personaDTO : dtos ) {
            list.add( toEntity( personaDTO ) );
        }

        return list;
    }

    @Override
    public Persona toEntity(PersonaDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Persona persona = new Persona();

        persona.setId( dto.getId() );
        persona.setNombre( dto.getNombre() );
        persona.setApellido( dto.getApellido() );
        persona.setDni( dto.getDni() );
        persona.setEmail( dto.getEmail() );
        persona.setFechaNacimiento( dto.getFechaNacimiento() );
        persona.setDomicilio( domicilioMapper.toEntity( dto.getDomicilio() ) );

        return persona;
    }
}
