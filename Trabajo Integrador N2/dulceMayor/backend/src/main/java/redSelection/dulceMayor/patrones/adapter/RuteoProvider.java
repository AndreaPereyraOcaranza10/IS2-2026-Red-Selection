package redSelection.dulceMayor.patrones.adapter;

import redSelection.dulceMayor.dto.TrayectoDTO;
import redSelection.dulceMayor.patrones.strategy.PlanOptimizado;
import redSelection.dulceMayor.patrones.strategy.PuntoRuta;

import java.util.List;

/**
 * PATRÓN: Adapter (estructural) - interfaz destino (Target).
 * Lo que el sistema necesita de un servicio de ruteo por calles, sin conocer cuál es ni su formato.
 * Ambos métodos lanzan ServicioExternoException si el servicio no responde.
 */
public interface RuteoProvider {

    /** Mejor camino en auto entre dos puntos. */
    TrayectoDTO trayecto(PuntoRuta desde, PuntoRuta hasta);

    /**
     * Orden de menor recorrido por calles: parte del primer punto de la lista (fijo) y termina donde convenga.
     * @param puntos origen primero, luego las paradas. Mínimo dos puntos.
     * @return el plan, con el orden de TODOS los puntos recibidos (el primero siempre es el origen).
     */
    PlanOptimizado recorridoOptimo(List<PuntoRuta> puntos);
}
