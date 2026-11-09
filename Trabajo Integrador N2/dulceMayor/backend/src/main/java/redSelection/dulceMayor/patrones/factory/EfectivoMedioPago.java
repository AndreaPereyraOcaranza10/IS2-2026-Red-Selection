package redSelection.dulceMayor.patrones.factory;

import org.springframework.stereotype.Component;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.model.Pago;

/**
 * Efectivo: se cobra al entregar la mercadería. A diferencia de los otros medios, lo puede confirmar también el
 * transportista (es quien tiene la plata en la mano). Que sea el transportista DUEÑO de esa entrega lo exige
 * EntregaService, no este método.
 */
@Component
public class EfectivoMedioPago implements MedioPago {

    @Override
    public TipoPago tipo() { return TipoPago.EFECTIVO; }

    @Override
    public boolean puedeConfirmar(Rol rol) {
        return rol == Rol.TRANSPORTISTA || MedioPago.super.puedeConfirmar(rol);
    }

    @Override
    public String instrucciones(Pago pago) {
        return "Abonás $" + pago.getMonto() + " en efectivo al recibir la mercadería.";
    }
}
