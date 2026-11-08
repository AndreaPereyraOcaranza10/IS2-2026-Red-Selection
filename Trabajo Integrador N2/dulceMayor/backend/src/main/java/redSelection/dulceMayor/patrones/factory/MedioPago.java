package redSelection.dulceMayor.patrones.factory;

import redSelection.dulceMayor.enums.EstadoPago;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.model.FacturaCliente;
import redSelection.dulceMayor.model.Pago;

/**
 * PATRÓN: Factory Method (creacional) + Strategy (comportamiento).
 * Cada medio de pago sabe CREAR su propio Pago (crearPago) y encapsula lo que lo distingue:
 * qué instrucciones recibe el cliente y quién puede confirmar el cobro. MedioPagoFactory elige cuál usar.
 * Sumar un medio nuevo = una clase nueva, sin tocar PagoService (principio Abierto/Cerrado).
 */
public interface MedioPago {

    TipoPago tipo();

    /** Qué tiene que hacer el cliente para pagar (se le muestra en su panel). */
    String instrucciones(Pago pago);

    /** Método de fábrica: el monto SIEMPRE sale de la factura, nunca de lo que mande el cliente. */
    default Pago crearPago(FacturaCliente factura) {
        return Pago.builder()
                .factura(factura)
                .tipoPago(tipo())
                .estado(EstadoPago.PENDIENTE)
                .monto(factura.getTotal())
                .build();
    }

    /** Paso posterior a guardar, cuando el Pago ya tiene id (ej.: armar el QR que lo referencia). */
    default void alGuardar(Pago pago) { }

    /** Quién puede dar el cobro por confirmado. Por defecto, la administración. */
    default boolean puedeConfirmar(Rol rol) {
        return rol.esAdministracion();
    }
}