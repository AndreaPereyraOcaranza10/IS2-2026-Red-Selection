package carrito.modelo;

import carrito.util.Json;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Carrito {
    public final String id;
    public final Usuario usuario;
    public final List<Detalle> detalles = new ArrayList<>();
    public BigDecimal subtotal = BigDecimal.ZERO;
    public BigDecimal envio = BigDecimal.ZERO;
    public BigDecimal total = BigDecimal.ZERO;
    public boolean eliminado;

    public Carrito(String id, Usuario usuario) {
        this.id = id;
        this.usuario = usuario;
    }

    public String toJson() {
        return "{\"id\":\"" + Json.escapar(id)
                + "\",\"total\":" + total
                + ",\"subtotal\":" + subtotal
                + ",\"envio\":" + envio
                + ",\"eliminado\":" + eliminado
                + ",\"usuario\":" + usuario.toJson()
                + ",\"detalles\":[" + String.join(",", detalles.stream().map(Detalle::toJson).toList()) + "]}";
    }
}
