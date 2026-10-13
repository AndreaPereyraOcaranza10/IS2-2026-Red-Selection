package redselection.cliente.patrones.builder;

import redselection.cliente.dto.AutorDTO;
import redselection.cliente.dto.LibroDTO;

import java.time.LocalDate;

public class LibroBuilderImpl implements LibroBuilder {

    private LibroDTO libro; // libro que queremos obtener como resultado

    public LibroBuilderImpl() {
        reiniciar();
    }

    @Override
    public void reiniciar() {
        this.libro = new LibroDTO(); // LibroDTO ya inicializa su lista de autores vacía
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
    public void construirPaginas(Integer paginas) {
        libro.setPaginas(paginas);
    }

    @Override
    public void construirFechaDevolucion(LocalDate fechaDevolucion) {
        libro.setFechaDevolucion(fechaDevolucion);
    }

    @Override
    public void agregarAutor(AutorDTO autor) {
        libro.getAutores().add(autor);
    }

    // Entrega el libro terminado y deja el builder listo para armar otro
    public LibroDTO obtenerResultado() {
        LibroDTO resultado = this.libro;
        this.reiniciar();
        return resultado;
    }
}