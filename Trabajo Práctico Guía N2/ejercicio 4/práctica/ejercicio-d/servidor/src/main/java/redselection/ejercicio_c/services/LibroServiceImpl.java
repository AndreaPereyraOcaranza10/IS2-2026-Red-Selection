package redselection.ejercicio_c.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import redselection.ejercicio_c.dto.LibroSolicitud;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.entities.Persona;
import redselection.ejercicio_c.patrones.estrategia.BuscadorLibros;
import redselection.ejercicio_c.patrones.estrategia.CriterioBusqueda;
import redselection.ejercicio_c.patrones.fabrica.LibroFactory;
import redselection.ejercicio_c.patrones.fabrica.LibroFactoryProvider;
import redselection.ejercicio_c.patrones.fabrica.TipoLibro;
import redselection.ejercicio_c.patrones.iterador.LibrosPorAutorIterator;
import redselection.ejercicio_c.repositories.BaseRepository;

import redselection.ejercicio_c.entities.Autor;
import redselection.ejercicio_c.repositories.AutorRepository;
import redselection.ejercicio_c.repositories.LibroRepository;
import redselection.ejercicio_c.repositories.PersonaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LibroServiceImpl extends BaseServiceImpl<Libro, Long> implements LibroService {

    private final AutorRepository autorRepository;
    private final LibroRepository libroRepository;
    private final PersonaRepository personaRepository;
    private final LibroFactoryProvider fabricas;
    private final BuscadorLibros buscador;

    public LibroServiceImpl(
            BaseRepository<Libro, Long> baseRepository,
            AutorRepository autorRepository,
            LibroRepository libroRepository,
            PersonaRepository personaRepository,
            LibroFactoryProvider fabricas,
            BuscadorLibros buscador) {

        super(baseRepository);
        this.autorRepository = autorRepository;
        this.libroRepository = libroRepository;
        this.personaRepository = personaRepository;
        this.fabricas = fabricas;
        this.buscador = buscador;
    }

    // El servicio no sabe cómo se arma un libro físico o digital: se lo pide a la fábrica del tipo.
    @Override
    public Libro crearLibro(TipoLibro tipo, LibroSolicitud solicitud) throws Exception {
        LibroFactory<?> fabrica = fabricas.obtener(tipo);
        Libro libro = fabrica.crearLibro(solicitud);
        return save(libro);
    }

    // El ABM de libros maneja solo los datos del libro. La asignación a una persona
    // (y su fecha de devolución) se maneja desde PersonaService.asignarLibro / desasignarLibro.
    @Override
    public Libro save(Libro libro) throws Exception {
        validarAutores(libro);
        libro.setFechaDevolucion(null);
        return super.save(libro);
    }

    private void validarAutores(Libro libro) {
        if (libro.getAutores() == null) {
            return;
        }

        List<Autor> autores = libro.getAutores().stream()
                .map(autor -> {
                    if (autor.getId() == null) {
                        throw new IllegalArgumentException(
                                "Cada autor debe tener un ID existente.");
                    }

                    return autorRepository.findById(autor.getId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "No existe el autor con ID: " + autor.getId()));
                })
                .collect(Collectors.toList());

        libro.setAutores(autores);
    }

    @Override
    public Libro update(Long id, Libro libro) throws Exception {
        Optional<Libro> existente = libroRepository.findById(id);
        if (existente.isEmpty()) {
            throw new Exception("No existe el libro con ID: " + id);
        }

        // Un libro físico no se convierte en digital (ni al revés): el tipo se define al crearlo
        if (!existente.get().getClass().equals(libro.getClass())) {
            throw new IllegalArgumentException("No se puede cambiar el tipo de un libro ya creado.");
        }

        validarAutores(libro);
        libro.setId(id);
        // Editar el libro no debe pisar el estado de préstamo que tenga
        libro.setFechaDevolucion(existente.get().getFechaDevolucion());
        return super.update(id, libro);
    }

    // Un libro asignado a una persona no se puede eliminar: primero hay que desasignarlo
    @Override
    @Transactional
    public boolean delete(Long id) throws Exception {
        Optional<Persona> titular = personaRepository.findFirstByLibrosId(id);
        if (titular.isPresent()) {
            throw new IllegalStateException("No se puede eliminar el libro porque está asignado a "
                    + titular.get().getNombre() + " " + titular.get().getApellido()
                    + ". Desasignalo primero.");
        }
        return super.delete(id);
    }

    @Override
    public List<Libro> listarDisponibles() {
        return libroRepository.findDisponibles();
    }

    // El servicio no sabe cómo se busca por título, género o autor: se lo pide al buscador,
    // que elige la estrategia según el criterio.
    @Override
    @Transactional
    public List<Libro> buscar(String criterio, String valor) {
        CriterioBusqueda criterioBusqueda = CriterioBusqueda.desde(criterio);
        return buscador.buscar(criterioBusqueda, valor, baseRepository.findAll());
    }

    @Override
    public List<Libro> listarPorAutor(Long autorId) {
        List<Libro> todosLosLibros = baseRepository.findAll();
        List<Libro> resultado = new ArrayList<>();

        LibrosPorAutorIterator iterador =
                new LibrosPorAutorIterator(todosLosLibros, autorId);

        while (iterador.hasNext()) {
            resultado.add(iterador.next());
        }

        return resultado;
    }

}