package carrito.modelo;

import carrito.util.Json;

public class Usuario {
    public final String id;
    public final String nombre;
    public final String apellido;
    public final String clave;
    public boolean eliminado;

    public Usuario(String id, String nombre, String apellido, String clave) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.clave = clave;
    }

    public String toJson() {
        return "{\"id\":\"" + Json.escapar(id)
                + "\",\"nombre\":\"" + Json.escapar(nombre)
                + "\",\"apellido\":\"" + Json.escapar(apellido)
                + "\",\"eliminado\":" + eliminado + "}";
    }
}
