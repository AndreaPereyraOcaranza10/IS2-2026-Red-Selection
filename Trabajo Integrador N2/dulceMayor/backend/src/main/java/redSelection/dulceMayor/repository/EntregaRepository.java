package redSelection.dulceMayor.repository;

import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.model.Entrega;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntregaRepository extends BaseRepository<Entrega> {
    Optional<Entrega> findByPedidoId(String pedidoId);
    List<Entrega> findByTransportistaIdAndFechaEntregaIsNull(String transportistaId);

    /** Historial de un transportista: entregas realizadas entre dos instantes, la más reciente primero. */
    List<Entrega> findByTransportistaIdAndFechaEntregaBetweenAndEliminadoFalseOrderByFechaEntregaDesc(
            String transportistaId, LocalDateTime desde, LocalDateTime hasta);
}