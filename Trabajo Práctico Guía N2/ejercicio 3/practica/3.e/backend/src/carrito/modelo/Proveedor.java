package carrito.modelo;

import carrito.util.Json;

public class Proveedor {
    public final String id;
    public final String nombre;
    public boolean eliminado;

    public Proveedor(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String toJson() {
        return "{\"id\":\"" + Json.escapar(id)
                + "\",\"nombre\":\"" + Json.escapar(nombre)
                + "\",\"eliminado\":" + eliminado + "}";
    }
}
