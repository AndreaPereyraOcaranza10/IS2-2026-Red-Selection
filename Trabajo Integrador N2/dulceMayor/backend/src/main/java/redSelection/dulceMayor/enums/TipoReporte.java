package redSelection.dulceMayor.enums;

import java.util.Locale;
import java.util.Optional;

/** Reportes que puede generar el sistema. VENTAS es global: solo el Jefe Administrativo (ver SecurityConfig). */
public enum TipoReporte {
    CLIENTES(false),
    PEDIDOS(false),
    ENVIOS(false),
    MONTOS(false),
    VENTAS(true);

    private final boolean global;

    TipoReporte(boolean global) {
        this.global = global;
    }

    public boolean esGlobal() {
        return global;
    }

    /** "pedidos", "PEDIDOS", " Pedidos " -> PEDIDOS. */
    public static Optional<TipoReporte> desdeTexto(String texto) {
        if (texto == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(texto.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}