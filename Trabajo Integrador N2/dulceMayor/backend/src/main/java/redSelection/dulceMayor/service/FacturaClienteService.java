package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.model.FacturaCliente;
import redSelection.dulceMayor.repository.FacturaClienteRepository;

import java.util.Optional;

/**
 * Acceso a facturas para otros servicios (así PagoService y EntregaService no tocan el repository ajeno).
 * Si Andrea ya tiene un FacturaClienteService con la facturación (BK-05), unir estos métodos ahí.
 */
@Service
public class FacturaClienteService {

    private final FacturaClienteRepository facturaRepository;

    public FacturaClienteService(FacturaClienteRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    @Transactional(readOnly = true)
    public FacturaCliente obtenerEntidad(String id) {
        return facturaRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura", id));
    }

    /** La factura de un pedido, si ya fue emitida (el pedido puede no estar facturado todavía). */
    @Transactional(readOnly = true)
    public Optional<FacturaCliente> buscarPorPedido(String pedidoId) {
        return facturaRepository.findByPedidoId(pedidoId).filter(f -> !f.isEliminado());
    }
}