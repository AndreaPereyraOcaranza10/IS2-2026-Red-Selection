package carrito;

import com.sun.net.httpserver.HttpServer;
import carrito.http.CarritoController;
import java.io.IOException;
import java.net.InetSocketAddress;

public class ServidorCarrito {
    public static void main(String[] args) throws IOException {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 3000;
        HttpServer server = HttpServer.create(new InetSocketAddress(puerto), 0);
        server.createContext("/", CarritoController::manejarRequest);
        server.setExecutor(null);
        server.start();
        System.out.println("Servidor iniciado en http://localhost:" + puerto);
    }
}
