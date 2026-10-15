package redselection.ejercicio_c.patrones.estrategia;

import redselection.ejercicio_c.entities.Libro;

import java.util.List;

// ESTRATEGIA (interfaz): define UNA operación, buscar, y cada implementación la resuelve a su manera.
// El que la usa (BuscadorLibros) no sabe si se busca por título, género o autor.
public interface BusquedaLibroStrategy {

    // Por qué criterio busca esta estrategia
    CriterioBusqueda getCriterio();

    // Devuelve los libros que coinciden con el valor buscado
    List<Libro> buscar(List<Libro> libros, String valor);
}