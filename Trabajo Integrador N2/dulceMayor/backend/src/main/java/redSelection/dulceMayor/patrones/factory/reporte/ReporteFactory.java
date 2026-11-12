package redSelection.dulceMayor.patrones.factory.reporte;

import org.springframework.stereotype.Component;
import redSelection.dulceMayor.enums.TipoReporte;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * PATRÓN: Factory Method (creador). Spring le inyecta todos los ReporteGenerador y los indexa por tipo.
 * Si falta un generador para algún TipoReporte, la aplicación no arranca (falla rápido, no en producción).
 */
@Component
public class ReporteFactory {

    private final Map<TipoReporte, ReporteGenerador> generadores = new EnumMap<>(TipoReporte.class);

    public ReporteFactory(List<ReporteGenerador> disponibles) {
        for (ReporteGenerador generador : disponibles) {
            if (generadores.put(generador.tipo(), generador) != null) {
                throw new IllegalStateException("Hay dos generadores para el reporte " + generador.tipo());
            }
        }
        for (TipoReporte tipo : TipoReporte.values()) {
            if (!generadores.containsKey(tipo)) {
                throw new IllegalStateException("Falta el generador del reporte " + tipo);
            }
        }
    }

    public ReporteGenerador crear(TipoReporte tipo) {
        return generadores.get(tipo);
    }
}