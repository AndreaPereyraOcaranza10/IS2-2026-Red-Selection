package redSelection.dulceMayor.patrones.strategy;

import java.util.List;

/**
 * PATRÓN: Strategy (comportamiento) - interfaz de la estrategia.
 * Define "cómo se ordenan las paradas pendientes de una ruta". RutaService depende solo de esta
 * interfaz: se puede cambiar el algoritmo agregando otra implementación, sin tocar el servicio.
 * Implementaciones: OsrmOptimizador (por calles, con respaldo) y VecinoMasCercanoOptimizador (línea recta).
 */
public interface OptimizadorRuta {

    /**
     * @param origen     punto de partida (ubicación actual, último comercio atendido o depósito); puede ser null,
     *                   en cuyo caso la primera parada recibida queda fija como inicio del recorrido.
     * @param pendientes paradas todavía sin atender.
     * @return el orden recomendado de visita y el recorrido resultante.
     */
    PlanOptimizado ordenar(PuntoRuta origen, List<PuntoRuta> pendientes);
}
