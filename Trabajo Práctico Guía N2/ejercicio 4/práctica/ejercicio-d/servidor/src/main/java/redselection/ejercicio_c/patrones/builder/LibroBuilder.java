package redselection.ejercicio_c.patrones.builder;

import redselection.ejercicio_c.entities.Autor;

import java.time.LocalDate;

public interface LibroBuilder {
    void reiniciar();
    void construirTitulo(String titulo);
    void construirFechaPublicacion(LocalDate fechaPublicacion);
    void construirGenero(String genero);
    void construirPaginas(int cantPaginas);
    void construirFechaDevolucion(LocalDate fechaDevolucion);
    void agregarAutor(Autor autor);
}