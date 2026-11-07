package redSelection.dulceMayor.dto;

import java.util.List;

/**
 * Resultado de optimizar una ruta: la ruta ya reordenada más el recorrido para dibujarlo.
 * `fuente` es "OSRM" (calles) o "ESTIMADA" (línea recta, cuando el servicio de ruteo no respondió).
 */
public record PlanRutaDTO(
        RutaDTO ruta,
        double distanciaKm,
        int duracionMin,
        List<double[]> geometria,
        String fuente
) {}
