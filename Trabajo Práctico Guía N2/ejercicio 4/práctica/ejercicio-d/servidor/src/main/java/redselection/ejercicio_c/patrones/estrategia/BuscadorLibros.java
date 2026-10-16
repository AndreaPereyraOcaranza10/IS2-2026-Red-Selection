package redselection.ejercicio_c.patrones.estrategia;

import org.springframework.stereotype.Component;
import redselection.ejercicio_c.entities.Libro;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// CONTEXTO del patrón Estrategia: recibe el criterio y delega la búsqueda en la estrategia que corresponde.
// Spring le inyecta TODAS las estrategias que existan (cada una es un @Component), así que para
// agregar un criterio nuevo alcanza con crear su estrategia: esta clase no se modifica.
@Component
public class BuscadorLibros {

    private final Map<CriterioBusqueda, BusquedaLibroStrategy> estrategias = new EnumMap<>(CriterioBusqueda.class);

    public BuscadorLibros(List<BusquedaLibroStrategy> estrategiasDisponibles) {
        for (BusquedaLibroStrategy estrategia : estrategiasDisponibles) {
            estrategias.put(estrategia.getCriterio(), estrategia);
        }
    }

    public List<Libro> buscar(CriterioBusqueda criterio, String valor, List<Libro> libros) {
        BusquedaLibroStrategy estrategia = estrategias.get(criterio);
        if (estrategia == null) {
            throw new IllegalArgumentException("No hay una estrategia de búsqueda para: " + criterio);
        }

        // Sin texto no hay nada que filtrar: se devuelven todos los libros
        if (valor == null || valor.isBlank()) {
            return new ArrayList<>(libros);
        }

        return estrategia.buscar(libros, valor);
    }
}