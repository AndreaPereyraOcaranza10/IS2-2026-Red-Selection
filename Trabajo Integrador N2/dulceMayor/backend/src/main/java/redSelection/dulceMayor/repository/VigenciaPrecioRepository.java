package redSelection.dulceMayor.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.model.VigenciaPrecio;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VigenciaPrecioRepository extends BaseRepository<VigenciaPrecio> {
    List<VigenciaPrecio> findByProductoIdAndEliminadoFalseOrderByFechaDesdeDesc(String productoId);

    /* vigencias que cubren la fecha dada, la más reciente primero. */
    @Query("""
            select v from VigenciaPrecio v
            where v.producto.id = :productoId
              and v.eliminado = false
              and v.fechaDesde <= :fecha
              and (v.fechaHasta is null or v.fechaHasta >= :fecha)
            order by v.fechaDesde desc
            """)
    List<VigenciaPrecio> vigentesEn(@Param("productoId") String productoId, @Param("fecha") LocalDate fecha);
}