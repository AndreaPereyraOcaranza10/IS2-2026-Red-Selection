package redSelection.dulceMayor.patrones.factory.reporte;

import redSelection.dulceMayor.dto.PeriodoReporte;
import redSelection.dulceMayor.dto.ReporteDTO;
import redSelection.dulceMayor.enums.TipoReporte;

/**
 * PATRÓN: Factory Method (producto). Cada tipo de reporte es un generador que sabe armar su propio ReporteDTO.
 * ReporteFactory entrega el generador que corresponde a un TipoReporte, así ReporteService no conoce las
 * clases concretas: sumar un reporte nuevo es crear una clase @Component y un valor en el enum.
 */
public interface ReporteGenerador {

    TipoReporte tipo();

    /** Se invoca dentro de una transacción de solo lectura (las relaciones LAZY ya se pueden recorrer). */
    ReporteDTO generar(PeriodoReporte periodo);
}