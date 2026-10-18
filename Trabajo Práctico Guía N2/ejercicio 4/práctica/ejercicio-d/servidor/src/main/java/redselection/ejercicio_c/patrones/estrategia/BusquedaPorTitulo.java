package redselection.ejercicio_c.patrones.estrategia;

import org.springframework.stereotype.Component;
import redselection.ejercicio_c.entities.Libro;

import java.util.List;

// Estrategia concreta: el título del libro contiene el texto buscado
@Component
public class BusquedaPorTitulo implements BusquedaLibroStrategy {

    @Override
    public CriterioBusqueda getCriterio() {
        return CriterioBusqueda.TITULO;
    }

    @Override
    public List<Libro> buscar(List<Libro> libros, String valor) {
        return libros.stream()
                .filter(libro -> TextoBusqueda.contiene(libro.getTitulo(), valor))
                .toList();
    }
}