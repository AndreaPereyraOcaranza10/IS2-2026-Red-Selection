package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.enums.TipoMovimientoStock;

import java.time.LocalDateTime;

public record MovimientoStockRespuestaDTO(String id, LocalDateTime fecha, TipoMovimientoStock tipoMovimiento,
                                          int movimiento, int cantidadActual) {}