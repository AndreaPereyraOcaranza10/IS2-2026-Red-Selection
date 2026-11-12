package redSelection.dulceMayor.patrones.adapter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Validación del header x-signature (formato "ts=...,v1=..."): HMAC-SHA256 del manifest
 * "id:[data.id en minúsculas];request-id:[x-request-id];ts:[ts];" con la clave secreta del webhook.
 * Es una defensa EXTRA: aunque falle o no esté configurada, nunca se acredita por lo que dice el webhook,
 * siempre se consulta de nuevo la orden a la API de Mercado Pago.
 */
public final class MercadoPagoFirma {

    private MercadoPagoFirma() { }

    public static boolean esValida(String xSignature, String xRequestId, String dataId, String secreto) {
        if (xSignature == null || secreto == null || secreto.isBlank() || dataId == null) return false;
        String ts = null;
        String v1 = null;
        for (String parte : xSignature.split(",")) {
            String[] kv = parte.trim().split("=", 2);
            if (kv.length != 2) continue;
            if (kv[0].equals("ts")) ts = kv[1];
            if (kv[0].equals("v1")) v1 = kv[1];
        }
        if (ts == null || v1 == null) return false;
        String manifest = "id:" + dataId.toLowerCase() + ";request-id:" + (xRequestId == null ? "" : xRequestId) + ";ts:" + ts + ";";
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String esperada = HexFormat.of().formatHex(mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(esperada.getBytes(StandardCharsets.UTF_8), v1.getBytes(StandardCharsets.UTF_8));
        } catch (java.security.GeneralSecurityException e) {
            return false;
        }
    }
}