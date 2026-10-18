package carrito.operaciones;

import carrito.modelo.Carrito;
import carrito.modelo.Detalle;
import carrito.util.Json;
import java.math.BigDecimal;

public abstract class OperacionCarritoTemplate {
    public final String ejecutar(ContextoOperacion contexto) {
        validar(contexto);
        antesDeProcesar(contexto);
        String mensaje = procesar(contexto);
        recalcularTotales(contexto.carrito);
        return crearRespuesta(contexto, mensaje);
    }

    protected void validar(ContextoOperacion contexto) {
        if (contexto.carrito == null || contexto.carrito.eliminado) {
            throw new IllegalArgumentException("El carrito no se encuentra disponible");
        }
    }

    protected void antesDeProcesar(ContextoOperacion contexto) {
    }

    protected abstract String procesar(ContextoOperacion contexto);

    protected void recalcularTotales(Carrito carrito) {
        carrito.subtotal = carrito.detalles.stream()
                .map(Detalle::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        carrito.envio = carrito.subtotal.compareTo(BigDecimal.ZERO) > 0
                && carrito.subtotal.compareTo(new BigDecimal("50000")) < 0
                        ? new BigDecimal("3500")
                        : BigDecimal.ZERO;
        carrito.total = carrito.subtotal.add(carrito.envio);
    }

    protected String crearRespuesta(ContextoOperacion contexto, String mensaje) {
        return "{\"mensaje\":\"" + Json.escapar(mensaje)
                + "\",\"carrito\":" + contexto.carrito.toJson() + "}";
    }
}
