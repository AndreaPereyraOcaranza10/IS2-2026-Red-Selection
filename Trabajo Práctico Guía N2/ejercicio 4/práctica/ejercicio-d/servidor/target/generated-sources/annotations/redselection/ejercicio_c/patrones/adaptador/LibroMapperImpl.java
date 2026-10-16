package redselection.ejercicio_c.patrones.adaptador;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redselection.ejercicio_c.dto.LibroDTO;
import redselection.ejercicio_c.dto.LibroSolicitud;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.entities.LibroDigital;
import redselection.ejercicio_c.entities.LibroFisico;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-16T11:50:57-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Microsoft)"
)
@Component
public class LibroMapperImpl implements LibroMapper {

    @Autowired
    private AutorMapper autorMapper;

    @Override
    public List<LibroDTO> toDtoList(List<Libro> entidades) {
        if ( entidades == null ) {
            return null;
        }

        List<LibroDTO> list = new ArrayList<LibroDTO>( entidades.size() );
        for ( Libro libro : entidades ) {
            list.add( toDto( libro ) );
        }

        return list;
    }

    @Override
    public List<Libro> toEntityList(List<LibroDTO> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<Libro> list = new ArrayList<Libro>( dtos.size() );
        for ( LibroDTO libroDTO : dtos ) {
            list.add( toEntity( libroDTO ) );
        }

        return list;
    }

    @Override
    public LibroSolicitud toSolicitud(LibroDTO dto) {
        if ( dto == null ) {
            return null;
        }

        LibroSolicitud libroSolicitud = new LibroSolicitud();

        libroSolicitud.setTitulo( dto.getTitulo() );
        libroSolicitud.setFechaPublicacion( dto.getFechaPublicacion() );
        libroSolicitud.setGenero( dto.getGenero() );
        libroSolicitud.setPaginas( dto.getPaginas() );
        libroSolicitud.setAutores( autorMapper.toEntityList( dto.getAutores() ) );
        libroSolicitud.setUbicacion( dto.getUbicacion() );
        libroSolicitud.setFormato( dto.getFormato() );
        libroSolicitud.setTamanioMb( dto.getTamanioMb() );
        libroSolicitud.setUrlDescarga( dto.getUrlDescarga() );

        return libroSolicitud;
    }

    @Override
    public LibroDTO fisicoADto(LibroFisico libro) {
        if ( libro == null ) {
            return null;
        }

        LibroDTO libroDTO = new LibroDTO();

        libroDTO.setId( libro.getId() );
        libroDTO.setTipo( libro.getTipo() );
        libroDTO.setTitulo( libro.getTitulo() );
        libroDTO.setFechaPublicacion( libro.getFechaPublicacion() );
        libroDTO.setGenero( libro.getGenero() );
        libroDTO.setPaginas( libro.getPaginas() );
        libroDTO.setFechaDevolucion( libro.getFechaDevolucion() );
        libroDTO.setAutores( autorMapper.toDtoList( libro.getAutores() ) );
        libroDTO.setUbicacion( libro.getUbicacion() );

        return libroDTO;
    }

    @Override
    public LibroDTO digitalADto(LibroDigital libro) {
        if ( libro == null ) {
            return null;
        }

        LibroDTO libroDTO = new LibroDTO();

        libroDTO.setId( libro.getId() );
        libroDTO.setTipo( libro.getTipo() );
        libroDTO.setTitulo( libro.getTitulo() );
        libroDTO.setFechaPublicacion( libro.getFechaPublicacion() );
        libroDTO.setGenero( libro.getGenero() );
        libroDTO.setPaginas( libro.getPaginas() );
        libroDTO.setFechaDevolucion( libro.getFechaDevolucion() );
        libroDTO.setAutores( autorMapper.toDtoList( libro.getAutores() ) );
        libroDTO.setFormato( libro.getFormato() );
        libroDTO.setTamanioMb( libro.getTamanioMb() );
        libroDTO.setUrlDescarga( libro.getUrlDescarga() );

        return libroDTO;
    }

    @Override
    public LibroFisico dtoAFisico(LibroDTO dto) {
        if ( dto == null ) {
            return null;
        }

        LibroFisico libroFisico = new LibroFisico();

        libroFisico.setId( dto.getId() );
        libroFisico.setTitulo( dto.getTitulo() );
        libroFisico.setFechaPublicacion( dto.getFechaPublicacion() );
        libroFisico.setGenero( dto.getGenero() );
        if ( dto.getPaginas() != null ) {
            libroFisico.setPaginas( dto.getPaginas() );
        }
        libroFisico.setFechaDevolucion( dto.getFechaDevolucion() );
        libroFisico.setAutores( autorMapper.toEntityList( dto.getAutores() ) );
        libroFisico.setUbicacion( dto.getUbicacion() );

        return libroFisico;
    }

    @Override
    public LibroDigital dtoADigital(LibroDTO dto) {
        if ( dto == null ) {
            return null;
        }

        LibroDigital libroDigital = new LibroDigital();

        libroDigital.setId( dto.getId() );
        libroDigital.setTitulo( dto.getTitulo() );
        libroDigital.setFechaPublicacion( dto.getFechaPublicacion() );
        libroDigital.setGenero( dto.getGenero() );
        if ( dto.getPaginas() != null ) {
            libroDigital.setPaginas( dto.getPaginas() );
        }
        libroDigital.setFechaDevolucion( dto.getFechaDevolucion() );
        libroDigital.setAutores( autorMapper.toEntityList( dto.getAutores() ) );
        libroDigital.setFormato( dto.getFormato() );
        libroDigital.setTamanioMb( dto.getTamanioMb() );
        libroDigital.setUrlDescarga( dto.getUrlDescarga() );

        return libroDigital;
    }
}
