package redselection.ejercicio_c.services;

import redselection.ejercicio_c.entities.Autor;

import java.sql.Date;

public interface LibroBuilder {
    void reiniciar();
    void construirTitulo(String titulo);
    void construirFechaPublicacion(Date fechaPublicacion);
    void construirGenero(String genero);
    void construirPaginas(int cantPaginas);
    void construirFechaDevolucion(Date fechaDevolucion);
    void agregarAutor(Autor autor);
}
