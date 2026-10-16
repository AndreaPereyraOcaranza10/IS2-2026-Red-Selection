package redselection.ejercicio_c.patrones.fabrica;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Elige qué fábrica usar según el tipo pedido.
// Spring le inyecta TODAS las fábricas que existan (cada una es un @Component), así que
// para agregar un tipo nuevo alcanza con crear su fábrica: este selector no se modifica.
@Component
public class LibroFactoryProvider {

    private final Map<TipoLibro, LibroFactory<?>> fabricas = new EnumMap<>(TipoLibro.class);

    public LibroFactoryProvider(List<LibroFactory<?>> fabricasDisponibles) {
        for (LibroFactory<?> fabrica : fabricasDisponibles) {
            fabricas.put(fabrica.getTipo(), fabrica);
        }
    }

    public LibroFactory<?> obtener(TipoLibro tipo) {
        LibroFactory<?> fabrica = fabricas.get(tipo);
        if (fabrica == null) {
            throw new IllegalArgumentException("No hay una fábrica registrada para el tipo de libro: " + tipo);
        }
        return fabrica;
    }
}