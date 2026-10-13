package redselection.ejercicio_c.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import redselection.ejercicio_c.dto.PersonaDTO;

import java.util.List;

public interface PersonaService extends BaseService<PersonaDTO, Long> {
    List<PersonaDTO> search(String filtro) throws Exception;
    //paginado
    Page<PersonaDTO> searchPaginado(String filtro, Pageable pageable) throws Exception;
}
