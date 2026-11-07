package redSelection.dulceMayor.repository;

import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.model.Promocion;

import java.util.Optional;

@Repository
public interface PromocionRepository extends BaseRepository<Promocion> {
    Optional<Promocion> findByCodigoIgnoreCaseAndEliminadoFalse(String codigo);
}

