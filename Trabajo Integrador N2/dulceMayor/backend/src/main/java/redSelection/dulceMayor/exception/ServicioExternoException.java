package redSelection.dulceMayor.exception;

/** Falla de un servicio externo (por ejemplo Georef) sin dato de respaldo. Se responde 503. */
public class ServicioExternoException extends RuntimeException {
    public ServicioExternoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
