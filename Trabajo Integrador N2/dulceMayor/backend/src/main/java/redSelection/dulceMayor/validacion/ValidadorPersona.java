package redSelection.dulceMayor.validacion;

import redSelection.dulceMayor.dto.PatronesValidacion;
import redSelection.dulceMayor.enums.TipoDocumento;
import redSelection.dulceMayor.exception.ReglaNegocioException;

import java.util.regex.Pattern;

/**
 * Reglas de datos personales compartidas por todos los ABM que cargan una persona (responsables de comercio,
 * perfil del cliente del portal y, cuando estén, empleados y usuarios). Se invocan desde la CAPA SERVICE:
 * el DTO frena lo evidente en la puerta, pero la regla de negocio vive acá y se aplica igual en el alta
 * y en la modificación. Todas devuelven el valor limpio (trim) o lanzan ReglaNegocioException (422).
 */
public final class ValidadorPersona {

    private static final int MAXIMO_NOMBRE = 60;
    private static final Pattern NOMBRE = Pattern.compile(PatronesValidacion.NOMBRE_PERSONA);
    private static final Pattern DNI = Pattern.compile(PatronesValidacion.DNI);
    private static final Pattern OTRO_DOCUMENTO = Pattern.compile(PatronesValidacion.DOCUMENTO);
    private static final Pattern TELEFONO = Pattern.compile(PatronesValidacion.TELEFONO);

    private ValidadorPersona() { }

    /** Nombres y apellidos: solo letras (con espacio, apóstrofo, punto o guion entre palabras). Sin números. */
    public static String nombre(String campo, String valor) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) {
            throw new ReglaNegocioException("El " + campo + " es obligatorio.");
        }
        if (limpio.length() > MAXIMO_NOMBRE || !NOMBRE.matcher(limpio).matches()) {
            throw new ReglaNegocioException("El " + campo + " solo puede tener letras (sin números ni símbolos, hasta "
                    + MAXIMO_NOMBRE + " caracteres).");
        }
        return limpio;
    }

    /** DNI: exactamente 8 números. Otros documentos (pasaporte): 6 a 9 letras o números. */
    public static String documento(TipoDocumento tipo, String valor) {
        String limpio = valor == null ? "" : valor.trim().toUpperCase();
        if (tipo == null || tipo == TipoDocumento.DNI) {
            if (!DNI.matcher(limpio).matches()) {
                throw new ReglaNegocioException("El DNI tiene que tener exactamente 8 números (sin puntos, espacios ni letras).");
            }
        } else if (!OTRO_DOCUMENTO.matcher(limpio).matches()) {
            throw new ReglaNegocioException("El documento debe tener entre 6 y 9 letras o números.");
        }
        return limpio;
    }

    /** Teléfono: 10 u 11 números, sin espacios, guiones ni letras. */
    public static String telefono(String valor) {
        String limpio = valor == null ? "" : valor.trim();
        if (!TELEFONO.matcher(limpio).matches()) {
            throw new ReglaNegocioException("El teléfono tiene que tener 10 u 11 números (sin espacios, guiones ni letras).");
        }
        return limpio;
    }

    /**
     * El correo de una persona ya cargada NO se modifica: es su usuario de acceso y a donde llegan los avisos.
     * Si no hay correo actual (dato viejo) no hay nada que proteger.
     */
    public static void correoSinCambios(String correoActual, String correoNuevo) {
        if (correoActual == null || correoActual.isBlank()) {
            return;
        }
        if (correoNuevo == null || !correoActual.trim().equalsIgnoreCase(correoNuevo.trim())) {
            throw new ReglaNegocioException("El correo no se puede modificar.");
        }
    }
}