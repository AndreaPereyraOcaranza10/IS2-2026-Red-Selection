package redSelection.dulceMayor.dto;

public record StockProductoEstadisticaDTO(String productoId, String codigo, String nombre, String marca,
                                          long stockActual, int stockMinimo, boolean bajoMinimo) { }
