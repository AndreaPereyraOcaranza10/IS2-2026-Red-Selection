package redselection.ejercicio_c.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import redselection.ejercicio_c.entities.Persona;
import redselection.ejercicio_c.repositories.BaseRepository;
import redselection.ejercicio_c.repositories.PersonaRepository;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.repositories.LibroRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


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

    // Los libros de una persona NO se manejan desde el alta ni desde la edición de la persona:
    // se asignan y desasignan con asignarLibro / desasignarLibro.
    @Override
    public Persona save(Persona persona) throws Exception {
        persona.setLibros(new ArrayList<>());
        return super.save(persona);
    }

    @Override
    public Persona update(Long id, Persona persona) throws Exception {
        Optional<Persona> existente = personaRepository.findById(id);
        if (existente.isEmpty()) {
            throw new Exception("No existe la persona con ID: " + id);
        }

        // Se ignora lo que venga en "libros": siempre se conservan los que ya tenía asignados
        persona.setLibros(existente.get().getLibros());
        persona.setId(id);

        return super.update(id, persona);
    }

    // Al borrar una persona sus libros siguen en el catálogo (no hay cascade), pero quedan devueltos
    @Override
    @Transactional
    public boolean delete(Long id) throws Exception {
        Optional<Persona> existente = personaRepository.findById(id);
        if (existente.isPresent()) {
            for (Libro libro : existente.get().getLibros()) {
                libro.setFechaDevolucion(null);
            }
        }
        return super.delete(id);
    }

    @Override
    @Transactional
    public Persona asignarLibro(Long personaId, Long libroId, LocalDate fechaDevolucion) {
        Optional<Persona> personaOpt = personaRepository.findById(personaId);
        if (personaOpt.isEmpty()) {
            throw new NoSuchElementException("No existe la persona con ID: " + personaId);
        }

        Optional<Libro> libroOpt = libroRepository.findById(libroId);
        if (libroOpt.isEmpty()) {
            throw new NoSuchElementException("No existe el libro con ID: " + libroId);
        }

        if (fechaDevolucion != null && fechaDevolucion.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de devolución no puede ser anterior a hoy.");
        }

        Persona persona = personaOpt.get();
        Libro libro = libroOpt.get();

        Optional<Persona> titular = personaRepository.findFirstByLibrosId(libroId);
        if (titular.isPresent()) {
            if (titular.get().getId().equals(personaId)) {
                throw new IllegalStateException("El libro ya está asignado a esta persona.");
            }
            throw new IllegalStateException("El libro ya está asignado a "
                    + titular.get().getNombre() + " " + titular.get().getApellido() + ".");
        }

        if (persona.getLibros() == null) {
            persona.setLibros(new ArrayList<>());
        }
        persona.getLibros().add(libro);
        libro.setFechaDevolucion(fechaDevolucion);

        return personaRepository.save(persona);
    }

    @Override
    @Transactional
    public Persona desasignarLibro(Long personaId, Long libroId) {
        Optional<Persona> personaOpt = personaRepository.findById(personaId);
        if (personaOpt.isEmpty()) {
            throw new NoSuchElementException("No existe la persona con ID: " + personaId);
        }

        Persona persona = personaOpt.get();
        Libro asignado = null;
        if (persona.getLibros() != null) {
            for (Libro libro : persona.getLibros()) {
                if (libroId.equals(libro.getId())) {
                    asignado = libro;
                    break;
                }
            }
        }

        if (asignado == null) {
            throw new NoSuchElementException("La persona no tiene asignado el libro con ID: " + libroId);
        }

        // Solo se rompe el vínculo: el libro sigue existiendo y vuelve a estar disponible
        persona.getLibros().remove(asignado);
        asignado.setFechaDevolucion(null);

        return personaRepository.save(persona);
    }

}