package redselection.cliente.patrones.builder;

import redselection.cliente.dto.AutorDTO;

import java.time.LocalDate;
import java.util.List;

public class LibroDirector {

    private LibroBuilder builder;

    public LibroDirector(LibroBuilder builder) {
        this.builder = builder;
    }

    public void cambiarBuilder(LibroBuilder builder) {
        this.builder = builder;
    }

    // Libro de catálogo: todos sus datos, sin préstamo
    public void construirLibroDeCatalogo(String titulo, LocalDate fechaPublicacion, String genero,
                                         Integer paginas, List<AutorDTO> autores) {
        builder.reiniciar();
        builder.construirTitulo(titulo);
        builder.construirFechaPublicacion(fechaPublicacion);
        builder.construirGenero(genero);
        builder.construirPaginas(paginas);
        autores.forEach(builder::agregarAutor);
    }

    // Libro prestado: el de catálogo más su fecha de devolución
    public void construirLibroPrestado(String titulo, LocalDate fechaPublicacion, String genero,
                                       Integer paginas, List<AutorDTO> autores, LocalDate fechaDevolucion) {
        construirLibroDeCatalogo(titulo, fechaPublicacion, genero, paginas, autores);
        builder.construirFechaDevolucion(fechaDevolucion);
    }
}
