package redSelection.dulceMayor.patrones.factory.reporte;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Ayudas comunes de los generadores: armar filas y dar formato a fechas y nombres. */
final class Celdas {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Celdas() { }

    static List<Object> fila(Object... celdas) {
        return new ArrayList<>(Arrays.asList(celdas));
    }

    static String fecha(LocalDateTime fecha) {
        return fecha == null ? "-" : fecha.format(FECHA_HORA);
    }

    /** Los ids son UUID largos: para mostrar alcanzan los primeros 8 (igual que el front). */
    static String idCorto(String id) {
        return id == null ? "" : id.substring(0, Math.min(8, id.length()));
    }

    static String nombre(String nombre, String apellido) {
        return ((nombre == null ? "" : nombre) + " " + (apellido == null ? "" : apellido)).trim();
    }
}