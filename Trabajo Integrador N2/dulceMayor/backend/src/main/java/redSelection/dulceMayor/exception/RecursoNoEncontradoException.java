package redSelection.dulceMayor.exception;

public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " no encontrado: " + id);
    }

    /** Con un mensaje para el usuario, cuando mostrar el id no le sirve (ej. "No encontramos ese punto de venta"). */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}