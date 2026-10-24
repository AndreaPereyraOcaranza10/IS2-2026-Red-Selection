package redselection.ejercicio_c.services;

import redselection.ejercicio_c.entities.Autor;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.patrones.builder.LibroBuilder;

import java.sql.Date;
import java.util.ArrayList;

public class LibroBuilderImpl implements LibroBuilder {

    private Libro libro; //libro que queremos obtener como resultado

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
    public void construirFechaPublicacion(Date fechaPublicacion) {
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
    public void construirFechaDevolucion(Date fechaDevolucion) {
        libro.setFechaDevolucion(fechaDevolucion.toLocalDate());
    }

    @Override
    public void agregarAutor(Autor autor) {
        libro.getAutores().add(autor);
    }

    public Libro obtenerResultado() {
        Libro resultado = this.libro;
        this.reiniciar();
        return resultado;
    }
}
