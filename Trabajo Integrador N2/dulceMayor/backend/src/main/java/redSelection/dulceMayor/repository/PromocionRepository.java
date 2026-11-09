package redSelection.dulceMayor.repository;

import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.model.Promocion;

import java.util.Optional;

@Repository
public interface PromocionRepository extends BaseRepository<Promocion> {
    Optional<Promocion> findByCodigoIgnoreCaseAndEliminadoFalse(String codigo);

    /** Incluye las dadas de baja: el código es único en la base aunque la promoción se haya eliminado. */
    Optional<Promocion> findByCodigoIgnoreCase(String codigo);
}

