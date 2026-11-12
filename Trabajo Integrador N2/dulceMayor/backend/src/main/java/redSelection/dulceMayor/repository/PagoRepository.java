package redSelection.dulceMayor.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.model.Pago;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends BaseRepository<Pago> {
    List<Pago> findByFacturaId(String facturaId);

    List<Pago> findByFacturaIdAndEliminadoFalse(String facturaId);

    List<Pago> findByPedidoIdAndEliminadoFalse(String pedidoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pago p where p.id = :id")
    Optional<Pago> buscarConBloqueo(@Param("id") String id);
}