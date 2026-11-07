package redSelection.dulceMayor.patrones.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import redSelection.dulceMayor.exception.ServicioExternoException;
import redSelection.dulceMayor.patrones.adapter.RuteoProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * PATRÓN: Strategy (comportamiento) - estrategia concreta, la que se usa por defecto (@Primary).
 * Ordena por el recorrido más corto por CALLES consultando el servicio de ruteo (a través del Adapter
 * RuteoProvider). Si el servicio no responde, cae en VecinoMasCercanoOptimizador: la optimización nunca falla,
 * y el plan indica en `fuente` si es "OSRM" o "ESTIMADA".
 */
@Primary
@Component
public class OsrmOptimizador implements OptimizadorRuta {

    private static final Logger log = LoggerFactory.getLogger(OsrmOptimizador.class);

    private final RuteoProvider ruteo;
    private final VecinoMasCercanoOptimizador respaldo;

    public OsrmOptimizador(RuteoProvider ruteo, VecinoMasCercanoOptimizador respaldo) {
        this.ruteo = ruteo;
        this.respaldo = respaldo;
    }

    @Override
    public PlanOptimizado ordenar(PuntoRuta origen, List<PuntoRuta> pendientes) {
        // Con un solo punto y sin origen no hay recorrido que calcular.
        if (pendientes.isEmpty() || (origen == null && pendientes.size() < 2)) {
            return respaldo.ordenar(origen, pendientes);
        }
        List<PuntoRuta> consulta = new ArrayList<>();
        if (origen != null) {
            consulta.add(origen);
        }
        consulta.addAll(pendientes);
        try {
            PlanOptimizado plan = ruteo.recorridoOptimo(consulta);
            List<String> orden = plan.orden().stream()
                    .filter(id -> origen == null || !id.equals(origen.id()))
                    .toList();
            return new PlanOptimizado(orden, plan.distanciaKm(), plan.duracionMin(), plan.geometria(), plan.fuente());
        } catch (ServicioExternoException ex) {
            log.warn("Ruteo por calles no disponible, se usa la estimación en línea recta: {}", ex.getMessage());
            return respaldo.ordenar(origen, pendientes);
        }
    }
}
