package redselection.ejercicio_c.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import redselection.ejercicio_c.entities.Persona;
import redselection.ejercicio_c.repositories.BaseRepository;
import redselection.ejercicio_c.repositories.PersonaRepository;

import java.util.List;


@Service
public class PersonaServiceImpl extends BaseServiceImpl<Persona, Long> implements PersonaService {

    @Autowired
    private PersonaRepository personaRepository;

    public PersonaServiceImpl(BaseRepository<Persona, Long> baseRepository) {
        super(baseRepository);
    }

    @Override
    public List<Persona> search(String filtro) throws Exception {
        try {
            //List<Persona> personas = personaRepository.findByNombreCOntainingOrApellidoContaining(filtro, filtro);
            //retrun personaRepository.searchNativo(filtro);
            return personaRepository.search(filtro);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Page<Persona> searchPaginado(String filtro, Pageable pageable) throws Exception {
        try {
            //Page<Persona> personas = personaRepository.findByNombreCOntainingOrApellidoContaining(filtro, pageable);
            //retrun personaRepository.searchNativo(filtro, pageable);
            return personaRepository.searchPaginado(filtro, pageable);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}