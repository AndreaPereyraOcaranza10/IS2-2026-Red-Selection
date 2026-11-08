package redSelection.dulceMayor.exception;

import org.springframework.security.authentication.LockedException;

/** Demasiados intentos fallidos de login: la cuenta queda bloqueada un rato. Se responde 423 con Retry-After. */
public class CuentaBloqueadaException extends LockedException {

    private final long segundosRestantes;

    public CuentaBloqueadaException(long segundosRestantes) {
        super("Demasiados intentos fallidos. Probá de nuevo en " + Math.max(1, (segundosRestantes + 59) / 60) + " minuto(s).");
        this.segundosRestantes = segundosRestantes;
    }

    public long getSegundosRestantes() { return segundosRestantes; }
}
