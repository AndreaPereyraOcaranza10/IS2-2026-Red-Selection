package redselection.ejercicio_c.patrones.builder;

import redselection.ejercicio_c.entities.Autor;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.patrones.fabrica.LibroFactory;

import java.time.LocalDate;
import java.util.ArrayList;

/*

PATRÓN BUILDER:

Construye un Libro paso a paso, asignando sus atributos y autores.

LibroDirector define las recetas de construcción y LibroBuilderImpl

ejecuta cada paso sobre el objeto en construcción.

obtenerResultado() devuelve el libro terminado y reinicia el builder.
*/

public class LibroBuilderImpl implements LibroBuilder {

    private final LibroFactory<? extends Libro> fabrica; // decide qué tipo de libro se va a armar
    private Libro libro; // libro que queremos obtener como resultado

    // Libro es abstracto: el builder le pide a la fábrica la instancia vacía del tipo que corresponda
    public LibroBuilderImpl(LibroFactory<? extends Libro> fabrica) {
        this.fabrica = fabrica;
        reiniciar();
    }

    @Override
    public void reiniciar() {
        this.libro = fabrica.nuevaInstancia();
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