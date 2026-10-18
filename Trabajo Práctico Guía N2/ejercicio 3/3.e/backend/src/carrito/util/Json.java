package carrito.util;

public final class Json {
    private Json() {}

    public static String escapar(String valor) {
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
