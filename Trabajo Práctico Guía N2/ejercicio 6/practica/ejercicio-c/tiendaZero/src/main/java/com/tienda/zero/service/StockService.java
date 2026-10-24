package com.tienda.zero.service;

import com.tienda.zero.model.DetalleCompra;
import com.tienda.zero.model.Stock;

import java.util.List;

public interface StockService {

    /** Registra el movimiento de stock de una línea de factura, con el signo de su factura. */
    Stock crearStock(String idDetalleFactura);

    void validar(String idDetalleFactura);

    /** Registra el movimiento contrario al de una línea de factura (anulación). */
    Stock revertirStock(String idDetalleFactura, String observacion);

    /** Registra una reserva o devolución generada por una compra de la tienda. */
    Stock registrarMovimiento(String idProducto, int movimiento, String observacion,
                              DetalleCompra detalleCompra);

    Stock buscarStock(String id);

    /** Último movimiento del producto (con su saldo). Falla si nunca tuvo movimientos. */
    Stock buscarStockActual(String idProducto);

    /** Saldo del producto: 0 si nunca tuvo movimientos. */
    int calcularStockActual(String idProducto);

    /** Historial de movimientos del producto, del más nuevo al más viejo. */
    List<Stock> listarStock(String idProducto);
}
