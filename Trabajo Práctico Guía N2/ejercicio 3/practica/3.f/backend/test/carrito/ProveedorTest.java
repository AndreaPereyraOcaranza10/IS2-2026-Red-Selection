package carrito;

import carrito.modelo.Proveedor;

/** Pruebas sin dependencias externas: ejecutar con java -cp backend/build carrito.ProveedorTest. */
public final class ProveedorTest {
    public static void main(String[] args) {
        new Proveedor("p", "Limite sur", "Direccion", -90, -180);
        new Proveedor("p", "Limite norte", "Direccion", 90, 180);
        new Proveedor("p", "Origen", "Direccion", 0, 0);
        for (double[] coordenadas : new double[][] {
                {90.01, 0}, {-90.01, 0}, {0, 180.01}, {0, -180.01},
                {Double.NaN, 0}, {0, Double.NaN},
                {Double.POSITIVE_INFINITY, 0}, {0, Double.NEGATIVE_INFINITY}}) {
            debeRechazar("Direccion", coordenadas[0], coordenadas[1]);
        }
        debeRechazar(null, 0, 0);
        debeRechazar("  ", 0, 0);
        Proveedor proveedor = new Proveedor("p", "Proveedor", "  Calle \"Centro\" 123  ", -34.6, -58.4);
        String json = proveedor.toJson();
        if (!json.contains("\"direccion\":\"Calle \\\"Centro\\\" 123\"")
                || !json.contains("\"latitud\":-34.6") || !json.contains("\"longitud\":-58.4")) {
            throw new AssertionError("La ubicacion no se serializa correctamente: " + json);
        }
        System.out.println("OK: limites, valores no finitos, direccion obligatoria y JSON de ubicacion.");
    }

    private static void debeRechazar(String direccion, double latitud, double longitud) {
        try {
            new Proveedor("p", "Proveedor", direccion, latitud, longitud);
        } catch (IllegalArgumentException esperado) {
            return;
        }
        throw new AssertionError("Se acepto una ubicacion invalida");
    }
}
