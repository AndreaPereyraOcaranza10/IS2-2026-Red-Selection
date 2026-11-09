package redSelection.dulceMayor.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.model.Usuario;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BK-16 / BK-15: matriz rol × endpoint. Prueba SOLO las reglas de SecurityConfig + JwtAuthFilter, con tokens
 * JWT reales. Un controller "sonda" responde 200 a cualquier /api/**, así que si llega un 200 es porque la
 * regla de seguridad dejó pasar el pedido (no hace falta base de datos ni mocks de servicios).
 */
// controllers = Sonda: sin esto @WebMvcTest carga TODOS los controllers reales, que necesitan services
// que este test no levanta (y además le quitarían rutas a la sonda).
@WebMvcTest(controllers = SeguridadEndpointsTest.Sonda.class)
@Import({SecurityConfig.class, JwtService.class, SeguridadEndpointsTest.Sonda.class, SeguridadEndpointsTest.SesionesDePrueba.class})
@TestPropertySource(properties = "app.jwt.secret=clave-de-prueba-de-al-menos-32-caracteres!!")
class SeguridadEndpointsTest {

    @RestController
    @RequestMapping("/api/**")
    static class Sonda {
        @RequestMapping
        String ok() { return "ok"; }
    }

    /** Sustituye a la consulta a la BD: un usuario es válido salvo que su id esté en "revocados". */
    @TestConfiguration
    static class SesionesDePrueba {
        static final java.util.Set<String> REVOCADOS = java.util.concurrent.ConcurrentHashMap.newKeySet();
        @Bean ControlDeSesion controlDeSesion() { return u -> !REVOCADOS.contains(u.getId()); }
    }

    @Autowired MockMvc mvc;
    @Autowired JwtService jwt;

    private String token(Rol rol, boolean primerIngreso, String empleadoId) {
        Usuario u = Usuario.builder().nombreUsuario("test").rol(rol).primerIngreso(primerIngreso).build();
        u.setId("u-" + rol);
        return "Bearer " + jwt.emitir(u, empleadoId);
    }

    private int estado(HttpMethod metodo, String url, String token) throws Exception {
        MockHttpServletRequestBuilder req = request(metodo, url);
        if (token != null) req.header("Authorization", token);
        return mvc.perform(req).andReturn().getResponse().getStatus();
    }

    private void esperar(int esperado, HttpMethod metodo, String url, String token) throws Exception {
        org.junit.jupiter.api.Assertions.assertEquals(esperado, estado(metodo, url, token),
                metodo + " " + url + " con " + (token == null ? "sin token" : token.substring(0, 20)));
    }

    // ---------- público ----------
    @org.junit.jupiter.api.BeforeEach
    void limpiar() { SesionesDePrueba.REVOCADOS.clear(); }

    @Test @DisplayName("Catálogo, ubicación e imágenes son públicos")
    void publicos() throws Exception {
        esperar(200, HttpMethod.GET, "/api/catalogo/productos", null);
        esperar(200, HttpMethod.GET, "/api/ubicacion/provincias", null);
        esperar(200, HttpMethod.GET, "/api/imagenes/abc", null);
    }

    // ---------- 401: sin token ----------
    @Test @DisplayName("Sin token: todo lo privado devuelve 401")
    void sinToken() throws Exception {
        esperar(401, HttpMethod.GET, "/api/productos", null);
        esperar(401, HttpMethod.POST, "/api/categorias", null);
        esperar(401, HttpMethod.POST, "/api/imagenes", null);
        esperar(401, HttpMethod.GET, "/api/puntos-venta", null);
        esperar(401, HttpMethod.GET, "/api/rutas/preventista/e1/hoy", null);
        esperar(401, HttpMethod.POST, "/api/auth/first-login/change-password", null);
    }

    @Test @DisplayName("Token inválido o adulterado: 401")
    void tokenInvalido() throws Exception {
        esperar(401, HttpMethod.GET, "/api/productos", "Bearer esto.no.es.un.jwt");
        esperar(401, HttpMethod.GET, "/api/productos", token(Rol.ADMINISTRATIVO, false, "a1") + "x");
    }

    // ---------- 403: rol equivocado ----------
    @Test @DisplayName("Productos, categorías e imágenes (escritura): solo ADMINISTRATIVO")
    void gestionSoloAdmin() throws Exception {
        for (Rol rol : new Rol[]{Rol.CLIENTE, Rol.PREVENTISTA, Rol.TRANSPORTISTA}) {
            String t = token(rol, false, "e1");
            esperar(403, HttpMethod.GET, "/api/productos", t);
            esperar(403, HttpMethod.POST, "/api/productos", t);
            esperar(403, HttpMethod.POST, "/api/categorias", t);
            esperar(403, HttpMethod.PUT, "/api/subcategorias/x", t);
            esperar(403, HttpMethod.DELETE, "/api/categorias/x", t);
            esperar(403, HttpMethod.POST, "/api/imagenes", t);
        }
        String admin = token(Rol.ADMINISTRATIVO, false, "a1");
        esperar(200, HttpMethod.POST, "/api/productos", admin);
        esperar(200, HttpMethod.POST, "/api/categorias", admin);
        esperar(200, HttpMethod.POST, "/api/imagenes", admin);
    }

    @Test @DisplayName("Categorías: lectura para PREVENTISTA y ADMINISTRATIVO, no para CLIENTE ni TRANSPORTISTA")
    void categoriasLectura() throws Exception {
        esperar(200, HttpMethod.GET, "/api/categorias", token(Rol.PREVENTISTA, false, "e1"));
        esperar(200, HttpMethod.GET, "/api/categorias", token(Rol.ADMINISTRATIVO, false, "a1"));
        esperar(403, HttpMethod.GET, "/api/categorias", token(Rol.CLIENTE, false, null));
        esperar(403, HttpMethod.GET, "/api/subcategorias", token(Rol.TRANSPORTISTA, false, "t1"));
    }

    @Test @DisplayName("Puntos de venta: preventista y admin; baja solo admin; transportista y cliente fuera")
    void puntosDeVenta() throws Exception {
        String prev = token(Rol.PREVENTISTA, false, "p1");
        esperar(200, HttpMethod.POST, "/api/puntos-venta", prev);
        esperar(200, HttpMethod.GET, "/api/puntos-venta", prev);
        esperar(403, HttpMethod.DELETE, "/api/puntos-venta/x", prev);
        esperar(200, HttpMethod.DELETE, "/api/puntos-venta/x", token(Rol.ADMINISTRATIVO, false, "a1"));
        esperar(403, HttpMethod.POST, "/api/puntos-venta", token(Rol.TRANSPORTISTA, false, "t1"));
        esperar(403, HttpMethod.POST, "/api/puntos-venta", token(Rol.CLIENTE, false, null));
    }

    @Test @DisplayName("Pedidos: preventista carga y consulta; cambiar estado solo admin; transportista fuera")
    void pedidos() throws Exception {
        String prev = token(Rol.PREVENTISTA, false, "p1");
        esperar(200, HttpMethod.GET, "/api/orders", prev);
        esperar(200, HttpMethod.POST, "/api/orders", prev);
        esperar(200, HttpMethod.GET, "/api/orders/x", prev);
        esperar(403, HttpMethod.PATCH, "/api/orders/x/status", prev);
        esperar(200, HttpMethod.PATCH, "/api/orders/x/status", token(Rol.ADMINISTRATIVO, false, "a1"));
        esperar(403, HttpMethod.GET, "/api/orders", token(Rol.TRANSPORTISTA, false, "t1"));
    }

    @Test @DisplayName("Promociones: el preventista solo valida códigos; el ABM es de administración")
    void promociones() throws Exception {
        String prev = token(Rol.PREVENTISTA, false, "p1");
        esperar(200, HttpMethod.POST, "/api/promociones/validar", prev);
        esperar(403, HttpMethod.GET, "/api/promociones", prev);
        esperar(403, HttpMethod.POST, "/api/promociones", prev);
        String admin = token(Rol.ADMINISTRATIVO, false, "a1");
        esperar(200, HttpMethod.POST, "/api/promociones", admin);
        esperar(200, HttpMethod.DELETE, "/api/promociones/x", admin);
        esperar(200, HttpMethod.POST, "/api/promociones/validar", admin);
        esperar(403, HttpMethod.POST, "/api/promociones/validar", token(Rol.TRANSPORTISTA, false, "t1"));
    }

    @Test @DisplayName("Responsables: buscar para preventista; ABM solo admin")
    void responsables() throws Exception {
        String prev = token(Rol.PREVENTISTA, false, "p1");
        esperar(200, HttpMethod.GET, "/api/responsables/buscar", prev);
        esperar(403, HttpMethod.GET, "/api/responsables", prev);
        esperar(403, HttpMethod.POST, "/api/responsables", prev);
        esperar(200, HttpMethod.POST, "/api/responsables", token(Rol.ADMINISTRATIVO, false, "a1"));
    }

    // ---------- rutas: rol + propiedad ----------
    @Test @DisplayName("Cada empleado ve solo SU ruta; el admin ve cualquiera")
    void rutaPropia() throws Exception {
        String prev = token(Rol.PREVENTISTA, false, "p1");
        esperar(200, HttpMethod.GET, "/api/rutas/preventista/p1/hoy", prev);
        esperar(403, HttpMethod.GET, "/api/rutas/preventista/p2/hoy", prev);          // ruta ajena
        esperar(403, HttpMethod.GET, "/api/rutas/transportista/p1/hoy", prev);        // rol equivocado
        String trans = token(Rol.TRANSPORTISTA, false, "t1");
        esperar(200, HttpMethod.GET, "/api/rutas/transportista/t1/hoy", trans);
        esperar(403, HttpMethod.GET, "/api/rutas/transportista/t2/hoy", trans);
        esperar(200, HttpMethod.GET, "/api/rutas/preventista/p9/hoy", token(Rol.ADMINISTRATIVO, false, "a1"));
        esperar(403, HttpMethod.GET, "/api/rutas/preventista/p1/hoy", token(Rol.CLIENTE, false, "p1"));
    }

    @Test @DisplayName("Visita solo PREVENTISTA, entrega solo TRANSPORTISTA")
    void visitaYEntrega() throws Exception {
        esperar(200, HttpMethod.POST, "/api/rutas/paradas/x/visita", token(Rol.PREVENTISTA, false, "p1"));
        esperar(403, HttpMethod.POST, "/api/rutas/paradas/x/visita", token(Rol.TRANSPORTISTA, false, "t1"));
        esperar(200, HttpMethod.POST, "/api/rutas/paradas/x/entrega", token(Rol.TRANSPORTISTA, false, "t1"));
        esperar(403, HttpMethod.POST, "/api/rutas/paradas/x/entrega", token(Rol.PREVENTISTA, false, "p1"));
        esperar(403, HttpMethod.POST, "/api/rutas/paradas/x/entrega", token(Rol.CLIENTE, false, null));
    }

    @Test @DisplayName("Reordenar/optimizar ruta: preventista o transportista; cliente no")
    void ordenYOptimizar() throws Exception {
        esperar(200, HttpMethod.PUT, "/api/rutas/r1/orden", token(Rol.PREVENTISTA, false, "p1"));
        esperar(200, HttpMethod.POST, "/api/rutas/r1/optimizar", token(Rol.TRANSPORTISTA, false, "t1"));
        esperar(403, HttpMethod.POST, "/api/rutas/r1/optimizar", token(Rol.CLIENTE, false, null));
    }

    @Test @DisplayName("Ruteo: empleados de campo y admin; cliente no")
    void ruteo() throws Exception {
        esperar(200, HttpMethod.GET, "/api/ruteo/trayecto", token(Rol.TRANSPORTISTA, false, "t1"));
        esperar(403, HttpMethod.GET, "/api/ruteo/trayecto", token(Rol.CLIENTE, false, null));
    }

    // ---------- primer ingreso ----------
    @Test @DisplayName("Con la clave inicial pendiente solo se puede usar /api/auth/**")
    void primerIngreso() throws Exception {
        for (Rol rol : new Rol[]{Rol.ADMINISTRATIVO, Rol.PREVENTISTA, Rol.TRANSPORTISTA}) {
            String t = token(rol, true, "e1");
            esperar(403, HttpMethod.GET, "/api/productos", t);
            esperar(403, HttpMethod.GET, "/api/rutas/preventista/e1/hoy", t);
            esperar(403, HttpMethod.GET, "/api/categorias", t);
            esperar(200, HttpMethod.POST, "/api/auth/first-login/change-password", t);
        }
    }

    // ---------- deny by default ----------
    @Test @DisplayName("Endpoint sin regla declarada: cerrado aunque estés logueado como admin")
    void denyByDefault() throws Exception {
        esperar(403, HttpMethod.GET, "/api/lo-que-no-existe", token(Rol.ADMINISTRATIVO, false, "a1"));
        esperar(401, HttpMethod.GET, "/api/lo-que-no-existe", null);
    }

    // ---------- JEFE_ADMINISTRATIVO (RoleHierarchy) ----------
    @Test @DisplayName("El Jefe hereda lo del Administrativo")
    void jefeHeredaAdministrativo() throws Exception {
        String jefe = token(Rol.JEFE_ADMINISTRATIVO, false, "j1");
        esperar(200, HttpMethod.POST, "/api/productos", jefe);
        esperar(200, HttpMethod.POST, "/api/categorias", jefe);
        esperar(200, HttpMethod.POST, "/api/imagenes", jefe);
        esperar(200, HttpMethod.DELETE, "/api/puntos-venta/x", jefe);
        esperar(200, HttpMethod.POST, "/api/responsables", jefe);
        esperar(200, HttpMethod.GET, "/api/rutas/preventista/p9/hoy", jefe);     // ve cualquier ruta
    }

    @Test @DisplayName("El Jefe hereda lo del Preventista (alta de puntos de venta, categorías)")
    void jefeHeredaPreventista() throws Exception {
        String jefe = token(Rol.JEFE_ADMINISTRATIVO, false, "j1");
        esperar(200, HttpMethod.POST, "/api/puntos-venta", jefe);
        esperar(200, HttpMethod.GET, "/api/categorias", jefe);
    }

    @Test @DisplayName("Usuarios, estadísticas y reportes globales: solo el Jefe")
    void soloJefe() throws Exception {
        for (String url : new String[]{"/api/usuarios", "/api/estadisticas/pedidos", "/api/reportes/globales/ventas"}) {
            esperar(200, HttpMethod.GET, url, token(Rol.JEFE_ADMINISTRATIVO, false, "j1"));
            esperar(403, HttpMethod.GET, url, token(Rol.ADMINISTRATIVO, false, "a1"));
            esperar(403, HttpMethod.GET, url, token(Rol.PREVENTISTA, false, "p1"));
            esperar(403, HttpMethod.GET, url, token(Rol.CLIENTE, false, null));
        }
    }

    @Test @DisplayName("El Jefe tampoco entra con la clave inicial pendiente")
    void jefePrimerIngreso() throws Exception {
        esperar(403, HttpMethod.GET, "/api/productos", token(Rol.JEFE_ADMINISTRATIVO, true, "j1"));
    }

    // ---------- revocación de tokens ----------
    @Test @DisplayName("Token revocado (logout / usuario desactivado): 401 aunque la firma sea válida")
    void tokenRevocado() throws Exception {
        String t = token(Rol.ADMINISTRATIVO, false, "a1");
        esperar(200, HttpMethod.GET, "/api/productos", t);
        SesionesDePrueba.REVOCADOS.add("u-" + Rol.ADMINISTRATIVO);
        esperar(401, HttpMethod.GET, "/api/productos", t);
    }

    @Test @DisplayName("Logout: requiere estar logueado")
    void logout() throws Exception {
        esperar(401, HttpMethod.POST, "/api/auth/logout", null);
        esperar(200, HttpMethod.POST, "/api/auth/logout", token(Rol.CLIENTE, false, null));
        esperar(200, HttpMethod.POST, "/api/auth/logout", token(Rol.PREVENTISTA, true, "p1"));   // también con clave pendiente
    }
}
