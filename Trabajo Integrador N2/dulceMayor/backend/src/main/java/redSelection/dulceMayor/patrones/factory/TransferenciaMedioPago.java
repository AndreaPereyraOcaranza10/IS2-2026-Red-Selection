package redSelection.dulceMayor.patrones.factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.model.Pago;

/** Transferencia: se le indica el alias de la empresa; administración confirma cuando se acredita. */
@Component
public class TransferenciaMedioPago implements MedioPago {

    private final String alias;

    public TransferenciaMedioPago(@Value("${app.pagos.alias:DULCE.MAYOR.MZA}") String alias) {
        this.alias = alias;
    }

    @Override
    public TipoPago tipo() { return TipoPago.TRANSFERENCIA; }

    @Override
    public String instrucciones(Pago pago) {
        return "Transferí $" + pago.getMonto() + " al alias " + alias
                + " y avisá a administración para que acredite el pago.";
    }
}
