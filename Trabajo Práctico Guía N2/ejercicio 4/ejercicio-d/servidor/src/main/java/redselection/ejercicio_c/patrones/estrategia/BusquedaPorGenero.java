package redselection.ejercicio_c.patrones.estrategia;

import org.springframework.stereotype.Component;
import redselection.ejercicio_c.entities.Libro;

import java.util.List;

// Estrategia concreta: el género del libro contiene el texto buscado
@Component
public class BusquedaPorGenero implements BusquedaLibroStrategy {

    @Override
    public CriterioBusqueda getCriterio() {
        return CriterioBusqueda.GENERO;
    }

    @Override
    public List<Libro> buscar(List<Libro> libros, String valor) {
        return libros.stream()
                .filter(libro -> TextoBusqueda.contiene(libro.getGenero(), valor))
                .toList();
    }
}