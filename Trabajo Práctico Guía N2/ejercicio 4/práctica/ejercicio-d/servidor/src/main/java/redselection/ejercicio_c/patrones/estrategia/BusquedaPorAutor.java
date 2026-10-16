package redselection.ejercicio_c.patrones.estrategia;

import org.springframework.stereotype.Component;
import redselection.ejercicio_c.entities.Autor;
import redselection.ejercicio_c.entities.Libro;

import java.util.List;
import java.util.Objects;

// Estrategia concreta: alguno de los autores del libro tiene el texto buscado en su nombre o apellido
// (acepta "Borges", "Jorge" o "Jorge Luis Borges"). Un libro sin autores nunca coincide.
@Component
public class BusquedaPorAutor implements BusquedaLibroStrategy {

    @Override
    public CriterioBusqueda getCriterio() {
        return CriterioBusqueda.AUTOR;
    }

    @Override
    public List<Libro> buscar(List<Libro> libros, String valor) {
        return libros.stream()
                .filter(libro -> libro.getAutores() != null
                        && libro.getAutores().stream().anyMatch(autor -> coincide(autor, valor)))
                .toList();
    }

    private boolean coincide(Autor autor, String valor) {
        String nombre = Objects.toString(autor.getNombre(), "");
        String apellido = Objects.toString(autor.getApellido(), "");
        String completo = nombre + " " + apellido;
        String invertido = apellido + " " + nombre;
        return TextoBusqueda.contiene(completo, valor) || TextoBusqueda.contiene(invertido, valor);
    }
}