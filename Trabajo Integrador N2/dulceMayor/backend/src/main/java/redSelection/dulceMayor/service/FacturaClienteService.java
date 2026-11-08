package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.model.FacturaCliente;
import redSelection.dulceMayor.repository.FacturaClienteRepository;

/**
 * Acceso a facturas para otros servicios (así PagoService no toca el repository ajeno).
 * Si Andrea ya tiene un FacturaClienteService con la facturación (BK-05), unir este método ahí.
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
}