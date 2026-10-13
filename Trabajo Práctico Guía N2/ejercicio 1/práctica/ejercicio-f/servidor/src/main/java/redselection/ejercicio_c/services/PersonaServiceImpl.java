package redselection.ejercicio_c.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import redselection.ejercicio_c.dto.PersonaDTO;
import redselection.ejercicio_c.entities.Persona;
import redselection.ejercicio_c.mappers.PersonaMapper;
import redselection.ejercicio_c.repositories.BaseRepository;
import redselection.ejercicio_c.repositories.PersonaRepository;

import java.util.List;


@Service
public class PersonaServiceImpl extends BaseServiceImpl<Persona, PersonaDTO, Long> implements PersonaService {

    @Autowired
    private PersonaRepository personaRepository;

    public PersonaServiceImpl(BaseRepository<Persona, Long> baseRepository, PersonaMapper personaMapper) {
        super(baseRepository, personaMapper);
    }

    @Override
    public List<PersonaDTO> search(String filtro) throws Exception {
        try {
            //List<Persona> personas = personaRepository.findByNombreCOntainingOrApellidoContaining(filtro, filtro);
            //retrun personaRepository.searchNativo(filtro);
            return baseMapper.toDTOList(personaRepository.search(filtro));
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Page<PersonaDTO> searchPaginado(String filtro, Pageable pageable) throws Exception {
        try {
            //Page<Persona> personas = personaRepository.findByNombreCOntainingOrApellidoContaining(filtro, pageable);
            //retrun personaRepository.searchNativo(filtro, pageable);
            return personaRepository.searchPaginado(filtro, pageable).map(baseMapper::toDTO);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
