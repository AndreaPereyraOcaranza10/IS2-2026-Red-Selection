package carrito.operaciones;

import carrito.modelo.Articulo;
import carrito.modelo.Carrito;
import java.math.BigDecimal;

public class ContextoOperacion {
    public final Carrito carrito;
    public final Articulo articulo;
    public BigDecimal totalAnterior = BigDecimal.ZERO;

    public ContextoOperacion(Carrito carrito, Articulo articulo) {
        this.carrito = carrito;
        this.articulo = articulo;
    }
}
