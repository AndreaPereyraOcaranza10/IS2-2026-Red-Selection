package redselection.cliente.patrones.builder;

import redselection.cliente.dto.AutorDTO;

import java.time.LocalDate;

public interface LibroBuilder {
    void reiniciar();
    void construirTitulo(String titulo);
    void construirFechaPublicacion(LocalDate fechaPublicacion);
    void construirGenero(String genero);
    void construirPaginas(Integer paginas);
    void construirFechaDevolucion(LocalDate fechaDevolucion);
    void agregarAutor(AutorDTO autor);
}