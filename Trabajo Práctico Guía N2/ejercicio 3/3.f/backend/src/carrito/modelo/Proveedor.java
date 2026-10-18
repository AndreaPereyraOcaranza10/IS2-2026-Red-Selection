package carrito.modelo;

import carrito.util.Json;

public class Proveedor {
    public final String id;
    public final String nombre;
    public final String direccion;
    public final double latitud;
    public final double longitud;
    public boolean eliminado;

    public Proveedor(String id, String nombre, String direccion, double latitud, double longitud) {
        if (direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException("Debe indicar la direccion del proveedor");
        }
        if (!Double.isFinite(latitud) || latitud < -90 || latitud > 90
                || !Double.isFinite(longitud) || longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("Las coordenadas del proveedor no son validas");
        }
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion.trim();
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public String toJson() {
        return "{\"id\":\"" + Json.escapar(id)
                + "\",\"nombre\":\"" + Json.escapar(nombre)
                + "\",\"direccion\":\"" + Json.escapar(direccion)
                + "\",\"latitud\":" + latitud
                + ",\"longitud\":" + longitud
                + ",\"eliminado\":" + eliminado + "}";
    }
}
