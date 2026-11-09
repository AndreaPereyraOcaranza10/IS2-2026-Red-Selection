package redSelection.dulceMayor.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.model.Stock;

import java.util.List;

@Repository
public interface StockRepository extends BaseRepository<Stock> {

    @Query("select coalesce(sum(s.movimiento), 0) from Stock s where s.producto.id = :productoId")
    long stockActual(@Param("productoId") String productoId);

    @Query("select s.producto.id as productoId, sum(s.movimiento) as cantidad from Stock s group by s.producto.id")
    List<StockPorProducto> stockPorProducto();

    @Query("""
            select p.id as productoId, p.codigo as codigo, p.nombre as nombre,
                   p.marca as marca, p.stockMinimo as stockMinimo,
                   coalesce(sum(s.movimiento), 0) as stockActual
            from Producto p left join Stock s on s.producto.id = p.id
            where p.eliminado = false
            group by p.id, p.codigo, p.nombre, p.marca, p.stockMinimo
            order by p.nombre
            """)
    List<StockProductoEstadistica> estadisticasStockPorProducto();

    List<Stock> findByProductoIdOrderByFechaDesc(String productoId);

    interface StockProductoEstadistica {
        String getProductoId();
        String getCodigo();
        String getNombre();
        String getMarca();
        Integer getStockMinimo();
        Long getStockActual();
    }
}
