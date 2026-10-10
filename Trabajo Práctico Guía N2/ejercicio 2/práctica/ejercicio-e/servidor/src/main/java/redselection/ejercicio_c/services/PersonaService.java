package redselection.ejercicio_c.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import redselection.ejercicio_c.entities.Persona;

import java.util.List;

public interface PersonaService extends BaseService<Persona, Long> {
    List<Persona> search(String filtro) throws Exception;
    //paginado
    Page<Persona> searchPaginado(String filtro, Pageable pageable) throws Exception;
}
