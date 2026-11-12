package redSelection.dulceMayor.patrones.factory;

import org.springframework.stereotype.Component;
import redSelection.dulceMayor.enums.EstadoPago;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.model.Pago;
import redSelection.dulceMayor.patrones.adapter.PasarelaPago;

/**
 * Billetera virtual (Mercado Pago). Al crear el pago le pide una orden a la pasarela (Adapter {@link PasarelaPago})
 * y guarda su referencia y el contenido del QR. El monto sale siempre del pedido. A diferencia de los otros medios,
 * NADIE confirma a mano: el cobro lo acredita únicamente la pasarela (PagoService consulta su estado).
 */
@Component
public class BilleteraVirtualMedioPago implements MedioPago {

    private final PasarelaPago pasarela;

    public BilleteraVirtualMedioPago(PasarelaPago pasarela) {
        this.pasarela = pasarela;
    }

    @Override
    public TipoPago tipo() { return TipoPago.BILLETERA_VIRTUAL; }

    @Override
    public void alGuardar(Pago pago) {
        PasarelaPago.OrdenPasarela orden = pasarela.crearOrden(pago.getId(), pago.getMonto(), "Pedido Dulce Mayor");
        pago.setReferenciaExterna(orden.referencia());
        pago.setCodigoQr(orden.contenidoQr());
    }

    @Override
    public String instrucciones(Pago pago) {
        if (pago.getEstado() == EstadoPago.CONFIRMADO) {
            return "El cliente ya pagó " + "$" + pago.getMonto() + " con Mercado Pago";
        }
        return "Escaneá el código QR con Mercado Pago (o tu billetera) para pagar $" + pago.getMonto() + ".";
    }

    /** Ningún rol confirma a mano un pago de billetera: lo acredita la pasarela. */
    @Override
    public boolean puedeConfirmar(Rol rol) { return false; }
}