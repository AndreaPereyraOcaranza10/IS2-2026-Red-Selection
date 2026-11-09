package redSelection.dulceMayor.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.enums.EstadoFactura;
import redSelection.dulceMayor.model.FacturaCliente;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaClienteRepository extends BaseRepository<FacturaCliente> {
    Optional<FacturaCliente> findByPedidoId(String pedidoId);
    List<FacturaCliente> findByPedidoClienteResponsableIdOrderByFechaFacturaDesc(String responsableId);

    @Query("select coalesce(max(f.numeroFactura), 0) from FacturaCliente f")
    long ultimoNumero();

    @Query("""
            select d.producto.id as productoId, d.producto.codigo as codigo,
                   d.producto.nombre as nombre, sum(d.cantidad) as unidadesVendidas
            from FacturaCliente f join f.detalles d
            where f.eliminado = false and f.estado <> :anulada
              and f.fechaFactura between :desde and :hasta
            group by d.producto.id, d.producto.codigo, d.producto.nombre
            order by sum(d.cantidad) desc, d.producto.nombre
            """)
    List<ProductoMasVendido> productosMasVendidos(@Param("desde") LocalDateTime desde,
                                                   @Param("hasta") LocalDateTime hasta,
                                                   @Param("anulada") EstadoFactura anulada,
                                                   Pageable pageable);

    interface ProductoMasVendido {
        String getProductoId();
        String getCodigo();
        String getNombre();
        Long getUnidadesVendidas();
    }
}
