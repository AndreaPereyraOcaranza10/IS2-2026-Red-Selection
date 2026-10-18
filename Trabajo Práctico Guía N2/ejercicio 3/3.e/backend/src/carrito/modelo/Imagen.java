package carrito.modelo;

import carrito.util.Json;

public class Imagen {
    public final String id;
    public final String nombre;
    public final String mime;
    public final String contenido;
    public boolean eliminado;

    public Imagen(String id, String nombre, String mime, String contenido) {
        this.id = id;
        this.nombre = nombre;
        this.mime = mime;
        this.contenido = contenido;
    }

    public String toJson() {
        return "{\"id\":\"" + Json.escapar(id)
                + "\",\"nombre\":\"" + Json.escapar(nombre)
                + "\",\"mime\":\"" + Json.escapar(mime)
                + "\",\"contenido\":\"" + Json.escapar(contenido)
                + "\",\"eliminado\":" + eliminado + "}";
    }
}
