package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.enums.TipoReporte;

import java.time.LocalDateTime;
import java.util.List;

/** Un reporte listo para mostrar en pantalla o exportar: una o más tablas con título y período. */
public record ReporteDTO(TipoReporte tipo, String titulo, String periodo, LocalDateTime generadoEn,
                         List<TablaReporteDTO> tablas) { }