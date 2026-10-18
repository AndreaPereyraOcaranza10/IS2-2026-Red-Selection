package carrito.http;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import carrito.modelo.Articulo;
import carrito.modelo.Carrito;
import carrito.modelo.Usuario;
import carrito.operaciones.OperacionCarritoTemplate;
import carrito.operaciones.ContextoOperacion;
import carrito.operaciones.AgregarArticuloOperacion;
import carrito.operaciones.QuitarArticuloOperacion;
import carrito.operaciones.FinalizarCompraOperacion;
import carrito.util.Json;
import carrito.datos.CatalogoDemo;

public final class CarritoController {
    private static final Usuario USUARIO = new Usuario("usr-1", "Ana", "Garcia", "1234");
    private static final List<Articulo> ARTICULOS = CatalogoDemo.crearArticulos();
    private static final Carrito CARRITO = new Carrito("car-1", USUARIO);

    private static final OperacionCarritoTemplate AGREGAR = new AgregarArticuloOperacion();
    private static final OperacionCarritoTemplate QUITAR = new QuitarArticuloOperacion();
    private static final OperacionCarritoTemplate FINALIZAR = new FinalizarCompraOperacion();

    public static void manejarRequest(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/api/")) {
                manejarApi(exchange, path);
                return;
            }
            ServidorEstatico.servir(exchange, path);
        } catch (IllegalArgumentException ex) {
            RespuestaHttp.json(exchange, 400, "{\"error\":\"" + Json.escapar(ex.getMessage()) + "\"}");
        } catch (Exception ex) {
            RespuestaHttp.json(exchange, 500, "{\"error\":\"Error interno del servidor\"}");
        }
    }

    private static void manejarApi(HttpExchange exchange, String path) throws IOException {
        String metodo = exchange.getRequestMethod();

        if ("GET".equals(metodo) && "/api/inicio".equals(path)) {
            RespuestaHttp.json(exchange, 200, jsonInicio());
            return;
        }

        if ("POST".equals(metodo) && "/api/carrito/agregar".equals(path)) {
            Articulo articulo = buscarArticulo(leerIdArticulo(exchange));
            RespuestaHttp.json(exchange, 200, AGREGAR.ejecutar(new ContextoOperacion(CARRITO, articulo)));
            return;
        }

        if ("POST".equals(metodo) && "/api/carrito/quitar".equals(path)) {
            Articulo articulo = buscarArticulo(leerIdArticulo(exchange));
            RespuestaHttp.json(exchange, 200, QUITAR.ejecutar(new ContextoOperacion(CARRITO, articulo)));
            return;
        }

        if ("POST".equals(metodo) && "/api/carrito/finalizar".equals(path)) {
            RespuestaHttp.json(exchange, 200, FINALIZAR.ejecutar(new ContextoOperacion(CARRITO, null)));
            return;
        }

        RespuestaHttp.json(exchange, 404, "{\"error\":\"Ruta API no encontrada\"}");
    }

    private static String leerIdArticulo(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Matcher matcher = Pattern.compile("\"idArticulo\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Debe indicar el id del articulo");
        }
        return matcher.group(1);
    }

    private static Articulo buscarArticulo(String idArticulo) {
        Optional<Articulo> articulo = ARTICULOS.stream()
                .filter(item -> item.id.equals(idArticulo) && !item.eliminado)
                .findFirst();

        if (articulo.isEmpty()) {
            throw new IllegalArgumentException("El articulo solicitado no existe");
        }

        return articulo.get();
    }

    private static String jsonInicio() {
        return "{\"usuario\":" + USUARIO.toJson()
                + ",\"articulos\":" + listaArticulosJson(ARTICULOS)
                + ",\"carrito\":" + CARRITO.toJson() + "}";
    }

    private static String listaArticulosJson(List<Articulo> articulos) {
        return "[" + String.join(",", articulos.stream().map(Articulo::toJson).toList()) + "]";
    }

}
