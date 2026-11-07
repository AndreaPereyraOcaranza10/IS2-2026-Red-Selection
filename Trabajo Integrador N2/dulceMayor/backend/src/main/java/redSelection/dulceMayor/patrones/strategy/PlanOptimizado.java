package redSelection.dulceMayor.patrones.strategy;

import java.util.List;

/**
 * Resultado de optimizar: el orden recomendado y el recorrido resultante.
 *
 * @param orden       ids de las paradas, en el orden de visita.
 * @param distanciaKm distancia total del recorrido (desde el origen, si hay).
 * @param duracionMin tiempo estimado en auto.
 * @param geometria   trazado como pares [latitud, longitud], listo para dibujar en el mapa.
 * @param fuente      "OSRM" si salió del servicio de ruteo por calles; "ESTIMADA" si es una aproximación en línea recta.
 */
public record PlanOptimizado(List<String> orden, double distanciaKm, int duracionMin, List<double[]> geometria, String fuente) {

    public static final String FUENTE_OSRM = "OSRM";
    public static final String FUENTE_ESTIMADA = "ESTIMADA";
}
