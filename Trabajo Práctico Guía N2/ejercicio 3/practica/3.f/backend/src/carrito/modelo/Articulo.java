package carrito.modelo;

import carrito.util.Json;
import java.math.BigDecimal;
import java.util.List;

public class Articulo {
    public final String id;
    public final String nombre;
    public final BigDecimal precio;
    public final Proveedor proveedor;
    public final List<Imagen> imagenes;
    public boolean eliminado;

    public Articulo(String id, String nombre, BigDecimal precio, Proveedor proveedor, List<Imagen> imagenes) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.proveedor = proveedor;
        this.imagenes = imagenes;
    }

    public String toJson() {
        return "{\"id\":\"" + Json.escapar(id)
                + "\",\"nombre\":\"" + Json.escapar(nombre)
                + "\",\"precio\":" + precio
                + ",\"eliminado\":" + eliminado
                + ",\"proveedor\":" + proveedor.toJson()
                + ",\"imagenes\":[" + String.join(",", imagenes.stream().map(Imagen::toJson).toList()) + "]}";
    }
}
