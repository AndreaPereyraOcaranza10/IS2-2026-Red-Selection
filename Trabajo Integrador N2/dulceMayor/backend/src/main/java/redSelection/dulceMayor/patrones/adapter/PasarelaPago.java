package redSelection.dulceMayor.patrones.adapter;

import redSelection.dulceMayor.exception.ReglaNegocioException;

import java.math.BigDecimal;

/**
 * PATRÓN: Adapter (estructural). Puerto hacia la pasarela de pago de la billetera virtual: el resto del sistema
 * (PagoService, BilleteraVirtualMedioPago) habla SOLO con esta interfaz y no sabe si detrás está Mercado Pago
 * o la pasarela simulada de demo. Cambiar de proveedor = una clase nueva, sin tocar la lógica de negocio.
 */
public interface PasarelaPago {

    /** Crea la orden de cobro en la pasarela. {@code pagoId} se usa como clave de idempotencia: repetir no duplica. */
    OrdenPasarela crearOrden(String pagoId, BigDecimal monto, String descripcion);

    /** Consulta SIEMPRE a la pasarela (nunca se confía en lo que diga un webhook o el navegador). */
    EstadoOrden consultar(String referencia);

    /** Cancela la orden en la pasarela (pedido anulado, vencido o pago reemplazado). */
    void cancelar(String referencia);

    /** Solo la pasarela simulada devuelve true: el front muestra el botón de demo. */
    default boolean esSimulada() { return false; }

    /** Solo la simulada: deja la orden como acreditada (equivale a que el cliente pague desde su billetera). */
    default void simularAcreditacion(String referencia) {
        throw new ReglaNegocioException("La acreditación simulada solo está disponible en modo demo.");
    }

    /** referencia = id de la orden en la pasarela; contenidoQr = el texto que se dibuja como QR. */
    record OrdenPasarela(String referencia, String contenidoQr) { }

    enum EstadoOrden { PENDIENTE, ACREDITADA, CANCELADA }
}