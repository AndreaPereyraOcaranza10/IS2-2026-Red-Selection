package redSelection.dulceMayor.patrones.factory;

import org.springframework.stereotype.Component;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.model.Pago;

/** Efectivo: se cobra al entregar la mercadería. (TR-02 va a sumar al transportista como quien confirma.) */
@Component
public class EfectivoMedioPago implements MedioPago {

    @Override
    public TipoPago tipo() { return TipoPago.EFECTIVO; }

    @Override
    public String instrucciones(Pago pago) {
        return "Abonás $" + pago.getMonto() + " en efectivo al recibir la mercadería.";
    }
}
