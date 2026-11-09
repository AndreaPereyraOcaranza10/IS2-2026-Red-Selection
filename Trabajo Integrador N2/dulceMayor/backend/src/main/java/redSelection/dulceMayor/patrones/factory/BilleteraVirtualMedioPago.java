package redSelection.dulceMayor.patrones.factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.model.Pago;

/**
 * Billetera virtual: se genera un QR que lleva a la pantalla de pago del cliente. El QR guarda solo la
 * referencia (id del pago); el monto sale siempre de la BD. Hasta tener un webhook de la billetera real
 * (candidato a Adapter), administración confirma el cobro.
 */
@Component
public class BilleteraVirtualMedioPago implements MedioPago {

    private final String urlBase;

    public BilleteraVirtualMedioPago(@Value("${app.pagos.qr-url:http://localhost:5173/cliente/pagos}") String urlBase) {
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
    }

    @Override
    public TipoPago tipo() { return TipoPago.BILLETERA_VIRTUAL; }

    @Override
    public void alGuardar(Pago pago) {
        pago.setCodigoQr(urlBase + "/" + pago.getId());
    }

    @Override
    public String instrucciones(Pago pago) {
        return "Escaneá el código QR con tu billetera virtual para pagar $" + pago.getMonto() + ".";
    }

    /** Apagada hasta integrar Mercado Pago (se reemplaza el QR por el checkout real vía un Adapter). */
    @Override
    public boolean disponible() { return false; }
}