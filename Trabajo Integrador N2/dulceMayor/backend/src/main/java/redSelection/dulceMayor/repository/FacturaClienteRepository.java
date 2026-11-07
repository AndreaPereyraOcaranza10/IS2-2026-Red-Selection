package redSelection.dulceMayor.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.model.FacturaCliente;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaClienteRepository extends BaseRepository<FacturaCliente> {
    Optional<FacturaCliente> findByPedidoId(String pedidoId);
    List<FacturaCliente> findByPedidoClienteResponsableIdOrderByFechaFacturaDesc(String responsableId);

    @Query("select coalesce(max(f.numeroFactura), 0) from FacturaCliente f")
    long ultimoNumero();
}