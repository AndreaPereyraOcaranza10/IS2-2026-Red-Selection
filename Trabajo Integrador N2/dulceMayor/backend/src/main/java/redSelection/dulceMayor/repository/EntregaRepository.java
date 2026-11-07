package redSelection.dulceMayor.repository;

import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.model.Entrega;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntregaRepository extends BaseRepository<Entrega> {
    Optional<Entrega> findByPedidoId(String pedidoId);
    List<Entrega> findByTransportistaIdAndFechaEntregaIsNull(String transportistaId);
}