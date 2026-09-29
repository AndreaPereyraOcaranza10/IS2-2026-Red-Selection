package com.tienda.zero.repository;

import com.tienda.zero.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StockRepository extends JpaRepository<Stock, String> {
    List<Stock> findByProductoIdAndEliminadoFalseOrderByFechaMovimientoDesc(String productoId);
    List<Stock> findByProductoIdAndEliminadoFalseOrderByFechaMovimientoAsc(String productoId);
    java.util.Optional<Stock> findFirstByProductoIdAndEliminadoFalseOrderByFechaMovimientoDesc(String productoId);
    @Query("select coalesce(sum(s.cantidad), 0) from Stock s where s.producto.id = :productoId and s.eliminado = false")
    Long calcularStockActual(@Param("productoId") String productoId);
}
