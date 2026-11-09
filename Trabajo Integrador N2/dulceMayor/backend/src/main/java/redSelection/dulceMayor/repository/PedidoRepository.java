package redSelection.dulceMayor.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.enums.EstadoPedido;
import redSelection.dulceMayor.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends BaseRepository<Pedido> {
    List<Pedido> findByClienteResponsableIdOrderByFechaHoraCargaDesc(String responsableId);
    List<Pedido> findByPreventistaIdOrderByFechaHoraCargaDesc(String preventistaId);
    List<Pedido> findByEstadoOrderByFechaHoraCargaAsc(EstadoPedido estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> buscarConBloqueo(@Param("id") String id);

    @Query("""
            select p.preventista.id as preventistaId,
                   concat(concat(p.preventista.nombre, ' '), p.preventista.apellido) as nombrePreventista,
                   year(p.fechaHoraCarga) as anio, month(p.fechaHoraCarga) as mes,
                   day(p.fechaHoraCarga) as dia, count(p.id) as cantidadPedidos,
                   sum(p.total) as montoPedidos
            from Pedido p
            where p.eliminado = false and p.estado <> :anulado
              and p.fechaHoraCarga between :desde and :hasta
            group by p.preventista.id, p.preventista.nombre, p.preventista.apellido,
                     year(p.fechaHoraCarga), month(p.fechaHoraCarga), day(p.fechaHoraCarga)
            order by year(p.fechaHoraCarga), month(p.fechaHoraCarga), day(p.fechaHoraCarga),
                     p.preventista.apellido, p.preventista.nombre
            """)
    List<PedidosPorDia> pedidosPorPreventistaDia(@Param("desde") LocalDateTime desde,
                                                   @Param("hasta") LocalDateTime hasta,
                                                   @Param("anulado") EstadoPedido anulado);

    @Query("""
            select p.preventista.id as preventistaId,
                   concat(concat(p.preventista.nombre, ' '), p.preventista.apellido) as nombrePreventista,
                   year(p.fechaHoraCarga) as anio, month(p.fechaHoraCarga) as mes,
                   count(p.id) as cantidadPedidos, sum(p.total) as montoPedidos
            from Pedido p
            where p.eliminado = false and p.estado <> :anulado
              and p.fechaHoraCarga between :desde and :hasta
            group by p.preventista.id, p.preventista.nombre, p.preventista.apellido,
                     year(p.fechaHoraCarga), month(p.fechaHoraCarga)
            order by year(p.fechaHoraCarga), month(p.fechaHoraCarga),
                     p.preventista.apellido, p.preventista.nombre
            """)
    List<PedidosPorMes> pedidosPorPreventistaMes(@Param("desde") LocalDateTime desde,
                                                   @Param("hasta") LocalDateTime hasta,
                                                   @Param("anulado") EstadoPedido anulado);

    @Query("""
            select p.preventista.id as preventistaId,
                   concat(concat(p.preventista.nombre, ' '), p.preventista.apellido) as nombrePreventista,
                   p.estado as estado, count(p.id) as cantidad
            from Pedido p
            where p.eliminado = false and p.fechaHoraCarga between :desde and :hasta
            group by p.preventista.id, p.preventista.nombre, p.preventista.apellido, p.estado
            order by p.preventista.apellido, p.preventista.nombre
            """)
    List<PedidosPorEstadoPreventista> pedidosPorEstadoPreventista(@Param("desde") LocalDateTime desde,
                                                                    @Param("hasta") LocalDateTime hasta);

    @Query("""
            select p.preventista.id as preventistaId, sum(p.total) as montoFacturado
            from Pedido p
            where p.eliminado = false and p.estado in :estadosFacturados
              and p.fechaHoraCarga between :desde and :hasta
            group by p.preventista.id
            """)
    List<VentasPreventista> ventasPorPreventista(@Param("desde") LocalDateTime desde,
                                                  @Param("hasta") LocalDateTime hasta,
                                                  @Param("estadosFacturados") List<EstadoPedido> estadosFacturados);

    interface PedidosPorDia {
        String getPreventistaId();
        String getNombrePreventista();
        Integer getAnio();
        Integer getMes();
        Integer getDia();
        Long getCantidadPedidos();
        BigDecimal getMontoPedidos();
    }

    interface PedidosPorMes {
        String getPreventistaId();
        String getNombrePreventista();
        Integer getAnio();
        Integer getMes();
        Long getCantidadPedidos();
        BigDecimal getMontoPedidos();
    }

    interface PedidosPorEstadoPreventista {
        String getPreventistaId();
        String getNombrePreventista();
        EstadoPedido getEstado();
        Long getCantidad();
    }

    interface VentasPreventista {
        String getPreventistaId();
        BigDecimal getMontoFacturado();
    }
}
