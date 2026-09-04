package com.ejercicio_c.biblioteca.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ejercicio_c.biblioteca.entities.Autor;
import com.ejercicio_c.biblioteca.entities.Editorial;
import com.ejercicio_c.biblioteca.entities.Libro;
import com.ejercicio_c.biblioteca.exceptions.ErrorServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import com.ejercicio_c.biblioteca.repositories.LibroRepository;


@Service
public class LibroService {

    @Autowired
    private LibroRepository repository;

    @Autowired
    private AutorService autorService;

    @Autowired
    private EditorialService editorialService;

    @Autowired
    private ImagenService imagenService;

    public void validar(Long isbn, String titulo, Integer ejemplares) throws ErrorServiceException {
        if (isbn == null || isbn.toString().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el ISBN");
        }
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el título");
        }
        if (ejemplares == null || ejemplares < 0) {
            throw new ErrorServiceException("La cantidad de ejemplares no puede ser menor que cero");
        }
    }

    @Transactional
    public void crearLibro(MultipartFile archivo, Long isbn, String titulo, Integer ejemplares, String idAutor,
                           String idEditorial) throws ErrorServiceException {
        try {
            validar(isbn, titulo, ejemplares);

            Autor autor = autorService.buscarAutor(idAutor);
            Editorial editorial = editorialService.buscarEditorial(idEditorial);

            try {
                Libro libroAux = repository.buscarLibroPorIsbn(isbn);
                if (libroAux != null && !libroAux.isEliminado()) {
                    throw new ErrorServiceException("Ya existe un libro con el ISBN indicado");
                }
            } catch (NoResultException ignored) {}

            try {
                Libro libroAux = repository.buscarLibroPorTituloAutorEditorial(titulo, idAutor, idEditorial);
                if (libroAux != null && !libroAux.isEliminado()) {
                    throw new ErrorServiceException("Ya existe un libro con el título, autor y editorial indicado");
                }
            } catch (NoResultException ignored) {}

            Libro libro = new Libro();
            libro.setId(UUID.randomUUID().toString());
            libro.setIsbn(isbn);
            libro.setTitulo(titulo);
            libro.setEjemplares(ejemplares);
            libro.setAutor(autor);
            libro.setEditorial(editorial);
            libro.setEliminado(false);
            if (archivo != null && !archivo.isEmpty()) {
                libro.setImagen(imagenService.crearImagen(archivo));
            }

            repository.save(libro);

        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrorServiceException("Error de sistema al crear libro");
        }
    }

    @Transactional
    public Libro modificarLibro(MultipartFile archivo, String idLibro, Long isbn, String titulo, Integer ejemplares,
                                String idAutor, String idEditorial) throws ErrorServiceException {
        try {
            validar(isbn, titulo, ejemplares);

            Autor autor = autorService.buscarAutor(idAutor);
            Editorial editorial = editorialService.buscarEditorial(idEditorial);
            Libro libro = buscarLibro(idLibro);

            try {
                Libro libroAux = repository.buscarLibroPorIsbn(isbn);
                if (libroAux != null && !libroAux.getId().equals(idLibro) && !libroAux.isEliminado()) {
                    throw new ErrorServiceException("Existe un libro con el ISBN indicado");
                }
            } catch (NoResultException ignored) {}

            try {
                Libro libroAux = repository.buscarLibroPorTituloAutorEditorial(titulo, idAutor, idEditorial);
                if (libroAux != null && !libroAux.getId().equals(idLibro) && !libroAux.isEliminado()) {
                    throw new ErrorServiceException("Existe un libro con el título, autor y editorial indicado");
                }
            } catch (NoResultException ignored) {}

            libro.setIsbn(isbn);
            libro.setTitulo(titulo);
            libro.setEjemplares(ejemplares);
            libro.setAutor(autor);
            libro.setEditorial(editorial);

            String idImagen = (libro.getImagen() != null) ? libro.getImagen().getId() : null;
            if (archivo != null && !archivo.isEmpty()) {
                libro.setImagen(imagenService.modificarImagen(idImagen, archivo));
            }

            return repository.save(libro);

        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrorServiceException("Error de sistema al modificar libro");
        }
    }

    @Transactional
    public void eliminarLibro(String idLibro) throws ErrorServiceException {
        Libro libro = buscarLibro(idLibro);
        libro.setEliminado(true);
        repository.save(libro);
    }

    public Libro buscarLibro(String idLibro) throws ErrorServiceException {
        if (idLibro == null || idLibro.trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el libro");
        }

        Optional<Libro> optional = repository.findById(idLibro);
        if (optional.isEmpty() || optional.get().isEliminado()) {
            throw new ErrorServiceException("No se encuentra el libro indicado");
        }

        return optional.get();
    }

    public List<Libro> listarLibro() throws ErrorServiceException {
        try {
            return repository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrorServiceException("Error de sistema al listar libros");
        }
    }

    public Libro buscarLibroPorIsbn(Long isbn) throws ErrorServiceException {
        if (isbn == null || isbn <= 0) {
            throw new ErrorServiceException("Debe indicar el ISBN");
        }
        return repository.buscarLibroPorIsbn(isbn);
    }

    public Libro buscarLibroPorTitulo(String titulo) throws ErrorServiceException {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el título");
        }
        return repository.buscarLibroPorTitulo(titulo);
    }

    public List<Libro> listarLibroPorAutor(String nombreAutor) throws ErrorServiceException {
        if (nombreAutor == null || nombreAutor.trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el nombre del autor");
        }
        try {
            return repository.listarLibroPorAutor(nombreAutor);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrorServiceException("Error de sistema al listar libros por autor");
        }
    }

    public List<Libro> listarLibroPorEditorial(String nombreEditorial) throws ErrorServiceException {
        if (nombreEditorial == null || nombreEditorial.trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el nombre de la editorial");
        }
        return repository.listarLibroPorEditorial(nombreEditorial);
    }

    public List<Libro> listarLibroPorAnio(Integer anio) throws ErrorServiceException {
        if (anio == null) {
            throw new ErrorServiceException("Debe indicar el año de publicación");
        }
        return repository.listarLibroPorAnio(anio);
    }

    public List<Libro> listarLibroPorFiltro(String filtro) throws ErrorServiceException {
        if (filtro == null || filtro.trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el criterio de búsqueda");
        }
        return repository.listarLibroPorFiltro(filtro);
    }
}
