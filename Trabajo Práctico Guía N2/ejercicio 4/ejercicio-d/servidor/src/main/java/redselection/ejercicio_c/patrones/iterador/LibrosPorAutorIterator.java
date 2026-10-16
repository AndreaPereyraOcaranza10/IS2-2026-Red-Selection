
package redselection.ejercicio_c.patrones.iterador;

import redselection.ejercicio_c.entities.Libro;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/*

PATRÓN ITERADOR:

LibrosPorAutorIterator recorre una lista de libros y devuelve

únicamente los que tienen asociado al autor indicado por su ID.

hasNext() busca y prepara el próximo libro válido; next() lo

devuelve. LibroServiceImpl utiliza el iterador para construir

la lista filtrada que devuelve el endpoint del servidor.
*/

public class LibrosPorAutorIterator implements Iterator<Libro> {

    private final Iterator<Libro> iterador;
    private final Long autorId;

    private Libro siguiente;
    private boolean preparado = false;

    public LibrosPorAutorIterator(List<Libro> libros, Long autorId) {
        this.iterador = libros.iterator();
        this.autorId = autorId;
    }

    @Override
    public boolean hasNext() {
        while (!preparado && iterador.hasNext()) {
            Libro libro = iterador.next();

            if (libro.getAutores() != null &&
                    libro.getAutores().stream()
                            .anyMatch(autor ->
                                    autorId.equals(autor.getId()))) {

                siguiente = libro;
                preparado = true;
            }
        }

        return preparado;
    }

    @Override
    public Libro next() {
        if (!hasNext()) {
            throw new NoSuchElementException(
                    "No hay más libros para este autor"
            );
        }

        Libro resultado = siguiente;
        siguiente = null;
        preparado = false;

        return resultado;
    }
}
