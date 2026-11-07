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

    List<Stock> findByProductoIdOrderByFechaDesc(String productoId);
}