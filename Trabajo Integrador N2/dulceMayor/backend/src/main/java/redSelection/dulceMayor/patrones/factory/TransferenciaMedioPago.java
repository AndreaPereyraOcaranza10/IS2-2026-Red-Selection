package redSelection.dulceMayor.patrones.factory;

import org.springframework.stereotype.Component;
import redSelection.dulceMayor.enums.TipoEmpresa;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.DulceMayor;
import redSelection.dulceMayor.model.FacturaCliente;
import redSelection.dulceMayor.model.Pago;
import redSelection.dulceMayor.repository.DulceMayorRepository;

/**
 Transferencia (simulada): no se mueve plata de verdad. Se le indica al cliente el alias de la casa central
 (el que está cargado en la empresa, no uno fijo en el código) y administración confirma cuando "se acredita".
 */
@Component
public class TransferenciaMedioPago implements MedioPago {

    private final DulceMayorRepository empresaRepository;

    public TransferenciaMedioPago(DulceMayorRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Override
    public TipoPago tipo() { return TipoPago.TRANSFERENCIA; }

    /** Falla al iniciar (422) si la empresa no tiene alias, en vez de dejar un pago sin instrucciones. */
    @Override
    public Pago crearPago(FacturaCliente factura) {
        aliasDeLaEmpresa();
        return MedioPago.super.crearPago(factura);
    }

    @Override
    public String instrucciones(Pago pago) {
        return "Transferí $" + pago.getMonto() + " al alias " + aliasDeLaEmpresa()
                + " y avisá a administración para que acredite el pago.";
    }

    private String aliasDeLaEmpresa() {
        return empresaRepository.findByEliminadoFalse().stream()
                .filter(d -> d.getTipoEmpresa() == TipoEmpresa.SEDE_CENTRAL)
                .map(DulceMayor::getAliasTransferencia)
                .filter(alias -> alias != null && !alias.isBlank())
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException("La empresa no tiene un alias de transferencia cargado."));
    }
}