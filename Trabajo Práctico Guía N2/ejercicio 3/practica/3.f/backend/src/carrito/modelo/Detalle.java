package carrito.modelo;

import carrito.util.Json;
import java.math.BigDecimal;

public class Detalle {
    public final String id;
    public final Articulo articulo;
    public int cantidad;
    public boolean eliminado;

    public Detalle(String id, Articulo articulo) {
        this.id = id;
        this.articulo = articulo;
        this.cantidad = 1;
    }

    public BigDecimal subtotal() {
        return eliminado ? BigDecimal.ZERO : articulo.precio.multiply(BigDecimal.valueOf(cantidad));
    }

    public String toJson() {
        return "{\"id\":\"" + Json.escapar(id)
                + "\",\"eliminado\":" + eliminado
                + ",\"cantidad\":" + cantidad
                + ",\"subtotal\":" + subtotal()
                + ",\"articulo\":" + articulo.toJson() + "}";
    }
}
