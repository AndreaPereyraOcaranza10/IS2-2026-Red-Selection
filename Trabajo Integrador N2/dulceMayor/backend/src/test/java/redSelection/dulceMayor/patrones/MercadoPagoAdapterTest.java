package redSelection.dulceMayor.patrones;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import redSelection.dulceMayor.patrones.adapter.MercadoPagoFirma;
import redSelection.dulceMayor.patrones.adapter.PasarelaMercadoPago;
import redSelection.dulceMayor.patrones.adapter.PasarelaPago.EstadoOrden;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

/** Traducción de estados de Mercado Pago y validación de firma del webhook (sin llamar a internet). */
class MercadoPagoAdapterTest {

    private static EstadoOrden estado(String status, String detalle) throws Exception {
        Method m = PasarelaMercadoPago.class.getDeclaredMethod("aEstado", String.class, String.class);
        m.setAccessible(true);
        return (EstadoOrden) m.invoke(null, status, detalle);
    }

    @Test @DisplayName("Solo 'processed + accredited' se considera pagado")
    void soloAcreditadoCuentaComoPagado() throws Exception {
        assertEquals(EstadoOrden.ACREDITADA, estado("processed", "accredited"));
        assertEquals(EstadoOrden.PENDIENTE, estado("created", "created"));
        assertEquals(EstadoOrden.PENDIENTE, estado("processed", "algo_raro"));
        assertEquals(EstadoOrden.PENDIENTE, estado(null, null));
    }

    @Test @DisplayName("Cancelada, vencida o reembolsada nunca se factura")
    void cancelacionesNoSeFacturan() throws Exception {
        assertEquals(EstadoOrden.CANCELADA, estado("canceled", "canceled"));
        assertEquals(EstadoOrden.CANCELADA, estado("expired", "expired"));
        assertEquals(EstadoOrden.CANCELADA, estado("refunded", "refunded"));
    }

    @Test @DisplayName("Sin las variables de entorno de Mercado Pago el sistema no arranca en modo real")
    void exigeCredenciales() {
        assertThrows(IllegalStateException.class,
                () -> new PasarelaMercadoPago("", "CAJA1", "https://api.mercadopago.com", "PT30M"));
        assertThrows(IllegalStateException.class,
                () -> new PasarelaMercadoPago("APP_USR-x", " ", "https://api.mercadopago.com", "PT30M"));
    }

    private static String firmar(String manifest, String secreto) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8)));
    }

    @Test @DisplayName("La firma del webhook se valida con HMAC-SHA256 y rechaza lo adulterado")
    void validaFirma() throws Exception {
        String v1 = firmar("id:ord01abc;request-id:req-1;ts:1700000000;", "secreto");
        String header = "ts=1700000000,v1=" + v1;

        assertTrue(MercadoPagoFirma.esValida(header, "req-1", "ORD01ABC", "secreto"));   // el id se compara en minúsculas
        assertFalse(MercadoPagoFirma.esValida(header, "req-2", "ORD01ABC", "secreto"));
        assertFalse(MercadoPagoFirma.esValida(header, "req-1", "ORD01ABC", "otro"));
        assertFalse(MercadoPagoFirma.esValida("ts=1,v1=00", "req-1", "ORD01ABC", "secreto"));
        assertFalse(MercadoPagoFirma.esValida(null, "req-1", "ORD01ABC", "secreto"));
    }
}