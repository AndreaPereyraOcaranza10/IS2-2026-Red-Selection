package redSelection.dulceMayor.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.enums.EstadoPedido;
import redSelection.dulceMayor.model.Pedido;

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
}