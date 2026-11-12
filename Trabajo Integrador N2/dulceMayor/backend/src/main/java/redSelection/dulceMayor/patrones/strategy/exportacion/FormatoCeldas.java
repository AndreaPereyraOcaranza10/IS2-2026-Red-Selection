package redSelection.dulceMayor.patrones.strategy.exportacion;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Texto de una celda para los formatos que no guardan números (PDF): plata como $ 1.234,50, cantidades con miles. */
final class FormatoCeldas {

    private static final Locale AR = Locale.forLanguageTag("es-AR");

    private FormatoCeldas() { }

    /** Un formateador por llamada: DecimalFormat no es seguro entre hilos. */
    static String texto(Object celda) {
        if (celda == null) {
            return "";
        }
        DecimalFormatSymbols simbolos = DecimalFormatSymbols.getInstance(AR);
        if (celda instanceof BigDecimal monto) {
            return new DecimalFormat("$ #,##0.00", simbolos).format(monto);
        }
        if (celda instanceof Long || celda instanceof Integer) {
            return new DecimalFormat("#,##0", simbolos).format(((Number) celda).longValue());
        }
        return celda.toString();
    }

    static boolean esNumero(Object celda) {
        return celda instanceof BigDecimal || celda instanceof Long || celda instanceof Integer;
    }
}