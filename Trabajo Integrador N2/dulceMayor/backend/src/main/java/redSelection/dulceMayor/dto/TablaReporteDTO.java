package redSelection.dulceMayor.dto;

import java.util.List;

/**
 * Una tabla de un reporte. Las celdas son String (texto), Long (cantidades) o BigDecimal (plata): así la pantalla
 * recibe números reales y el Excel puede sumarlos, y el PDF les da formato. `totales` tiene una celda por columna
 * (o está vacía si la tabla no lleva fila de totales).
 */
public record TablaReporteDTO(String nombre, List<String> columnas, List<List<Object>> filas, List<Object> totales) { }