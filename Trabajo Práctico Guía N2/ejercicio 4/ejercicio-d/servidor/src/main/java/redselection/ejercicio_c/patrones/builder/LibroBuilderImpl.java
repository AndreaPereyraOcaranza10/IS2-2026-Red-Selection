package redselection.ejercicio_c.patrones.builder;

import redselection.ejercicio_c.entities.Autor;
import redselection.ejercicio_c.entities.Libro;

import java.time.LocalDate;
import java.util.ArrayList;

public class LibroBuilderImpl implements LibroBuilder {

    private Libro libro; // libro que queremos obtener como resultado

    public LibroBuilderImpl() {
        reiniciar();
    }

    @Override
    public void reiniciar() {
        this.libro = new Libro();
        this.libro.setAutores(new ArrayList<>());
    }

    @Override
    public void construirTitulo(String titulo) {
        libro.setTitulo(titulo);
    }

    @Override
    public void construirFechaPublicacion(LocalDate fechaPublicacion) {
        libro.setFechaPublicacion(fechaPublicacion);
    }

    @Override
    public void construirGenero(String genero) {
        libro.setGenero(genero);
    }

    @Override
    public void construirPaginas(int cantPaginas) {
        libro.setPaginas(cantPaginas);
    }

    @Override
    public void construirFechaDevolucion(LocalDate fechaDevolucion) {
        libro.setFechaDevolucion(fechaDevolucion);
    }

    @Override
    public void agregarAutor(Autor autor) {
        libro.getAutores().add(autor);
    }

    // No está en la interfaz porque el tipo del resultado depende de cada builder
    public Libro obtenerResultado() {
        Libro resultado = this.libro;
        this.reiniciar();
        return resultado;
    }
}