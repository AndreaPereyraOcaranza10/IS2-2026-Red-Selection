/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import com.mycompany.minimarket.Stock;
import service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    @Autowired
    private StockService stockService;

    // ALTA DE STOCK PARA UN PRODUCTO: POST /api/stocks/producto/{productoId}
    @PostMapping("/producto/{productoId}")
    public ResponseEntity<Stock> agregarStock(@PathVariable Long productoId, @RequestBody Stock stock) {
        Stock nuevoStock = stockService.agregarStockAProducto(productoId, stock);
        return ResponseEntity.ok(nuevoStock);
    }

    // LECTURA POR PRODUCTO: GET /api/stocks/producto/{productoId}
    @GetMapping("/producto/{productoId}")
    public List<Stock> obtenerPorProducto(@PathVariable Long productoId) {
        return stockService.obtenerStocksPorProducto(productoId);
    }

    // MODIFICACIÓN: PUT /api/stocks/{stockId}
    @PutMapping("/{stockId}")
    public ResponseEntity<Stock> actualizarStock(@PathVariable Long stockId, @RequestBody Stock stock) {
        Stock actualizado = stockService.actualizarStock(stockId, stock);
        return ResponseEntity.ok(actualizado);
    }

    // BAJA: DELETE /api/stocks/{stockId}
    @DeleteMapping("/{stockId}")
    public ResponseEntity<Void> eliminarStock(@PathVariable Long stockId) {
        stockService.eliminarStock(stockId);
        return ResponseEntity.noContent().build();
    }
}