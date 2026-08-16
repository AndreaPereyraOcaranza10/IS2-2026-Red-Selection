/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import com.mycompany.minimarket.Producto;
import com.mycompany.minimarket.Stock;
import repository.ProductoRepository;
import repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // Alta de Stock asignado a un producto existente
    public Stock agregarStockAProducto(Long productoId, Stock stock) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + productoId));

        stock.setProducto(producto);
        return stockRepository.save(stock);
    }

    // Obtener todos los stocks de un producto
    public List<Stock> obtenerStocksPorProducto(Long productoId) {
        return stockRepository.findByProductoId(productoId);
    }

    // Modificación de cantidad o depósito de un registro de stock
    public Stock actualizarStock(Long stockId, Stock detallesStock) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock no encontrado con id: " + stockId));

        stock.setCantidad(detallesStock.getCantidad());
        stock.setDeposito(detallesStock.getDeposito());

        return stockRepository.save(stock);
    }

    // Baja / Eliminar registro de Stock
    public void eliminarStock(Long stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock no encontrado con id: " + stockId));
        stockRepository.delete(stock);
    }
}