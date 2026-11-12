// PuntoVentaAltaEvento.java
package redSelection.dulceMayor.service;

import redSelection.dulceMayor.model.ClienteMinorista;

/**
 * PATRÓN: Observer. Se publica al dar de alta un punto de venta con preventista asignado. RutaService lo escucha y
 * suma el comercio a la ruta de hoy del preventista: así PuntoVentaService no depende de RutaService
 * (y RutaService sí puede pedirle los comercios a PuntoVentaService sin dependencia circular).
 */
public record PuntoVentaAltaEvento(String preventistaId, ClienteMinorista cliente) { }
