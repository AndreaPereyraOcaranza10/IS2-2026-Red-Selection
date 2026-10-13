package redselection.ejercicio_c.services;

import org.springframework.stereotype.Service;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.patrones.iterador.LibrosPorAutorIterator;
import redselection.ejercicio_c.repositories.BaseRepository;

import redselection.ejercicio_c.entities.Autor;
import redselection.ejercicio_c.repositories.AutorRepository;
import redselection.ejercicio_c.repositories.LibroRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LibroServiceImpl extends BaseServiceImpl<Libro, Long> implements LibroService {

    private final AutorRepository autorRepository;

    public LibroServiceImpl(
            BaseRepository<Libro, Long> baseRepository,
            AutorRepository autorRepository) {

        super(baseRepository);
        this.autorRepository = autorRepository;
    }

    @Override
    public Libro save(Libro libro) throws Exception {
        validarAutores(libro);
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
        validarAutores(libro);
        libro.setId(id);
        return super.update(id, libro);
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
