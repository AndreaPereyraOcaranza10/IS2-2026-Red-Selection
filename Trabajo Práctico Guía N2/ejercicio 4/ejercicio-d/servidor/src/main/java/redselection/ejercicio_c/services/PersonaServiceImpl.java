package redselection.ejercicio_c.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import redselection.ejercicio_c.entities.Persona;
import redselection.ejercicio_c.repositories.BaseRepository;
import redselection.ejercicio_c.repositories.PersonaRepository;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.repositories.LibroRepository;
import java.util.stream.Collectors;

import java.util.List;


@Service
public class PersonaServiceImpl extends BaseServiceImpl<Persona, Long> implements PersonaService {

    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private LibroRepository libroRepository;

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

    @Override
    public Persona update(Long id, Persona persona) throws Exception {
        Persona existente = personaRepository.findById(id)
                .orElseThrow(() -> new Exception(
                        "No existe la persona con ID: " + id));

        if (persona.getLibros() != null) {
            List<Libro> librosExistentes = persona.getLibros().stream()
                    .map(libro -> {
                        if (libro.getId() == null) {
                            throw new IllegalArgumentException(
                                    "Cada libro debe tener un ID existente.");
                        }

                        return libroRepository.findById(libro.getId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "No existe el libro con ID: "
                                                + libro.getId()));
                    })
                    .collect(Collectors.toList());

            persona.setLibros(librosExistentes);
        } else {
            persona.setLibros(existente.getLibros());
        }

        persona.setId(id);

        return super.update(id, persona);
    }

}