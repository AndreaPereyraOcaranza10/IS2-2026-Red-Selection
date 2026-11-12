package redSelection.dulceMayor.patrones.strategy.exportacion;

import redSelection.dulceMayor.dto.ReporteDTO;
import redSelection.dulceMayor.enums.FormatoReporte;

/**
 * PATRÓN: Strategy. Cada formato de salida (PDF, Excel) es una estrategia intercambiable que convierte el mismo
 * ReporteDTO en bytes. ReporteService elige la estrategia según el formato pedido, sin if/else por formato:
 * sumar CSV o HTML es una clase nueva, sin tocar los generadores ni el service.
 */
public interface ExportadorReporte {

    FormatoReporte formato();

    byte[] exportar(ReporteDTO reporte);
}