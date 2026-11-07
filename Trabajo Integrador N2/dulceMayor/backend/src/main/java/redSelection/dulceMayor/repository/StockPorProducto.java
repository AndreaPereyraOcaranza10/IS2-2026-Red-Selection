package redSelection.dulceMayor.repository;

/*
Proyección: stock actual de cada producto (suma de movimientos).
Interfaz que le dice a SpringData qué columnsas queres traer de una consulta cuando no necesitas
la entidad completa
Esta consulta no nos trae la fila del stock completa sino pares (porductoID, cantidad)
*/
public interface StockPorProducto {
    String getProductoId();
    Long getCantidad();
}