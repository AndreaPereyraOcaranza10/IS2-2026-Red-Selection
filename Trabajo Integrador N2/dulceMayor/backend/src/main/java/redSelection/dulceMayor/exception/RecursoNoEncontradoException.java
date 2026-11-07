package redSelection.dulceMayor.exception;

public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " no encontrado: " + id);
    }
}