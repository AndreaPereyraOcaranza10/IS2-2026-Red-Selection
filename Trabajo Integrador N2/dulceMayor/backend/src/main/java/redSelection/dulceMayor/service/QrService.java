package redSelection.dulceMayor.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * Generación matricial del código QR con ZXing ("Zebra Crossing"). Recibe el TEXTO que entrega la pasarela de pago
 * (no inventa el contenido) y lo dibuja como imagen PNG lista para mostrar en el portal del cliente.
 */
@Service
public class QrService {

    private static final int LADO_PX = 320;

    /** Devuelve un data URI ("data:image/png;base64,...") para usar directo en un &lt;img src&gt;. */
    public String generarPngBase64(String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("No hay contenido para generar el QR.");
        }
        try {
            BitMatrix matriz = new QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, LADO_PX, LADO_PX,
                    Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                            EncodeHintType.MARGIN, 2,
                            EncodeHintType.CHARACTER_SET, "UTF-8"));
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matriz, "PNG", salida);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(salida.toByteArray());
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("No se pudo generar el código QR.", e);
        }
    }
}