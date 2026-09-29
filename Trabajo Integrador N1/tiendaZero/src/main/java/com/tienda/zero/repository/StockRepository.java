package com.tienda.zero.repository;

import com.tienda.zero.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {

    // Saldo actual de un producto: suma de todos sus movimientos (0 si no tiene ninguno)
    @Query("select coalesce(sum(s.movimiento), 0L) from Stock s "
            + "where s.detalleFactura.producto.id = :idProducto and s.eliminado = false")

    Long calcularStockActual(@Param("idProducto") String idProducto);

    // Cuántos movimientos generó un detalle: 0 = todavía no movió, 1 = movió, 2 = ya fue revertido
    long countByDetalleFacturaIdAndEliminadoFalse(String idDetalleFactura);

    Optional<Stock> findFirstByDetalleFacturaProductoIdAndEliminadoFalseOrderByFechaDesc(String idProducto);

    List<Stock> findByDetalleFacturaProductoIdAndEliminadoFalseOrderByFechaDesc(String idProducto);
}
