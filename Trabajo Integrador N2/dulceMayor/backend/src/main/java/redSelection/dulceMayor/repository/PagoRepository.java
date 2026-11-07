package redSelection.dulceMayor.repository;

import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.model.Pago;

import java.util.List;

@Repository
public interface PagoRepository extends BaseRepository<Pago> {
    List<Pago> findByFacturaId(String facturaId);
}
