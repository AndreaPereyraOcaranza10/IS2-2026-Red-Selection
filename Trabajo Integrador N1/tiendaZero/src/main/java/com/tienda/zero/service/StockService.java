package com.tienda.zero.service;

import com.tienda.zero.model.*;
import com.tienda.zero.repository.ProductoRepository;
import com.tienda.zero.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor
public class StockService {
    private final StockRepository stocks;
    private final ProductoRepository productos;

    public int calcularStockActual(String productoId) {
        return Math.toIntExact(stocks.calcularStockActual(productoId));
    }

    public List<Stock> listarStockActual(String productoId) {
        return stocks.findByProductoIdAndEliminadoFalseOrderByFechaMovimientoDesc(productoId);
    }

    public List<Stock> listarStock() {
        return stocks.findAll();
    }

    public Stock buscarStock(String id) {
        return stocks.findById(id).filter(stock -> !stock.isEliminado())
                .orElseThrow(() -> new IllegalArgumentException("Movimiento de stock no encontrado"));
    }

    public Stock buscarStockActual(String productoId) {
        return stocks.findFirstByProductoIdAndEliminadoFalseOrderByFechaMovimientoDesc(productoId)
                .orElseThrow(() -> new IllegalArgumentException("El producto todavía no tiene movimientos de stock"));
    }

    public void validar(String productoId, int cantidad) {
        if (productoId == null || productoId.isBlank()) throw new IllegalArgumentException("El producto es obligatorio");
        if (cantidad == 0) throw new IllegalArgumentException("La cantidad de movimiento no puede ser cero");
        if (!productos.existsById(productoId)) throw new IllegalArgumentException("Producto no encontrado");
    }

    @Transactional
    public void eliminarStock(String id, String observacion) {
        Stock movimiento = buscarStock(id);
        movimiento.setEliminado(true);
        movimiento.setObservacion(observacion);
        int saldo = 0;
        for (Stock vigente : stocks.findByProductoIdAndEliminadoFalseOrderByFechaMovimientoAsc(movimiento.getProducto().getId())) {
            saldo += vigente.getCantidad();
            vigente.setCantidadActual(saldo);
        }
    }

    @Transactional
    public Stock registrarMovimiento(String productoId, int cantidad, String observacion,
                                     OrdenCompraProveedor ordenProveedor, DetalleCompra detalleCompra) {
        if (cantidad == 0) throw new IllegalArgumentException("El movimiento de stock no puede ser cero");
        Producto producto = productos.findByIdForUpdate(productoId)
                .filter(p -> !p.isEliminado())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        int actual = calcularStockActual(productoId);
        int nuevoActual = actual + cantidad;
        if (nuevoActual < 0) throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre());
        return stocks.save(Stock.builder()
                .producto(producto)
                .cantidad(cantidad)
                .cantidadActual(nuevoActual)
                .observacion(observacion)
                .ordenCompraProveedor(ordenProveedor)
                .detalleCompra(detalleCompra)
                .fechaMovimiento(LocalDateTime.now())
                .build());
    }
}
