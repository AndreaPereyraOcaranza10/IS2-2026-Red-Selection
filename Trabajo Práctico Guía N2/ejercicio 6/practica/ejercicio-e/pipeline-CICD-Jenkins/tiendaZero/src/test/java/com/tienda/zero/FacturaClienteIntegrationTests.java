package com.tienda.zero;

import com.tienda.zero.enums.*;
import com.tienda.zero.model.*;
import com.tienda.zero.repository.*;
import com.tienda.zero.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FacturaClienteIntegrationTests {
    @Autowired private MockMvc mvc;
    @Autowired private FacturaClienteService facturas;
    @Autowired private FacturaClienteRepository facturasRepository;
    @Autowired private FlujoCompraService flujo;
    @Autowired private StockService stock;
    @Autowired private ProductoService productos;
    @Autowired private UsuarioService usuarios;
    @Autowired private PersonaRepository personas;
    @Autowired private NacionalidadRepository nacionalidades;
    @MockitoBean private MercadoPagoService mercadoPago;
    @MockitoBean private CorreoService correos;

    private Usuario cliente() {
        Usuario usuario = usuarios.crearUsuario("factura-" + UUID.randomUUID() + "@zero.test", "clave123", TipoUsuario.CLIENTE);
        personas.saveAndFlush(Cliente.builder().usuario(usuario).nombre("Ana").apellido("Perez")
                .numeroDocumento("12345678").tipoDocumento(TipoDocumento.DNI).sexo(Sexo.OTRO)
                .fechaNacimiento(Date.valueOf("1990-01-01")).direccionEstadia("Calle Uno 100")
                .nacionalidad(nacionalidades.findAll().getFirst()).build());
        return usuario;
    }

    private Producto producto() {
        return productos.listarProductoActivo().stream().filter(p -> stock.calcularStockActual(p.getId()) >= 4)
                .findFirst().orElseThrow();
    }

    private OrdenCompra comprar(Usuario usuario, Producto producto, TipoPago pago) {
        flujo.agregarAlCarrito(usuario.getNombreUsuario(), producto.getId(), 2);
        return flujo.crearOrdenCliente(usuario.getNombreUsuario(), "Calle Uno 100", pago);
    }

    @Test
    void emiteUnaFacturaAutomaticaConDatosCongeladosSinDuplicarStockNiFactura() throws Exception {
        Usuario usuario = cliente();
        Producto producto = producto();
        int inicial = stock.calcularStockActual(producto.getId());
        String nombreProducto = producto.getNombre();
        OrdenCompra orden = comprar(usuario, producto, TipoPago.BILLETERA_VIRTUAL);
        FacturaCliente factura = facturasRepository.findByOrdenCompraId(orden.getId()).orElseThrow();
        assertTrue(factura.getNumeroFactura() > 0);
        assertEquals("Ana", factura.getNombreCliente());
        assertEquals("Perez", factura.getApellidoCliente());
        assertEquals(usuario.getNombreUsuario(), factura.getCorreoCliente());
        assertFalse(factura.isCorreoPendiente());
        assertEquals(orden.getTotal().doubleValue(), factura.getTotalPagado());
        assertEquals(2, factura.getDetalles().getFirst().getCantidad());
        assertEquals(inicial - 2, stock.calcularStockActual(producto.getId()));
        assertEquals(factura.getId(), facturas.emitirAutomatica(orden).getId());
        assertEquals(inicial - 2, stock.calcularStockActual(producto.getId()));

        personas.findByUsuarioId(usuario.getId()).orElseThrow().setApellido("Otro apellido");
        producto.setNombre("Producto actualizado");
        assertEquals("Perez", factura.getApellidoCliente());
        assertEquals(nombreProducto, factura.getDetalles().getFirst().getNombreProducto());
        mvc.perform(get("/orders").with(user(usuario.getNombreUsuario()).roles("CLIENTE")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("/orders/facturas/" + factura.getId())));
        mvc.perform(get("/orders/facturas/{id}", factura.getId()).with(user(usuario.getNombreUsuario()).roles("CLIENTE")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Ana Perez")))
                .andExpect(content().string(containsString(nombreProducto)));
        verify(mercadoPago, never()).crearCheckout(any());
    }

    @Test
    void lasFacturasSonPrivadasYElAdministradorPuedeConsultarlas() throws Exception {
        Usuario usuario = cliente();
        OrdenCompra orden = comprar(usuario, producto(), TipoPago.BILLETERA_VIRTUAL);
        FacturaCliente factura = facturasRepository.findByOrdenCompraId(orden.getId()).orElseThrow();
        mvc.perform(get("/orders/facturas/{id}", factura.getId()).with(user("otro@zero.test").roles("CLIENTE")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/admin/facturas/clientes/{id}", factura.getId()).with(user(usuario.getNombreUsuario()).roles("CLIENTE")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/admin/facturas/clientes/{id}", factura.getId()).with(user("administrativo@tiendazero.com").roles("ADMINISTRATIVO")))
                .andExpect(status().isOk());
        mvc.perform(get("/admin/facturas").with(user("administrativo@tiendazero.com").roles("ADMINISTRATIVO")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Ana Perez")))
                .andExpect(content().string(containsString("Proveedores")));
    }

    @Test
    void compraAdministrativaAbreFormularioYConservaLosDatosCargados() throws Exception {
        Usuario usuario = usuarios.crearUsuario("admin-factura-" + UUID.randomUUID() + "@zero.test", "clave123", TipoUsuario.JEFE);
        flujo.agregarAlCarrito(usuario.getNombreUsuario(), producto().getId(), 2);
        mvc.perform(post("/checkout").with(user(usuario.getNombreUsuario()).roles("JEFE")).with(csrf())
                        .param("address", "Calle Uno 100").param("formaPago", "EFECTIVO"))
                .andExpect(status().is3xxRedirection());
        OrdenCompra orden = flujo.listarPedidosUsuario(usuario.getNombreUsuario()).getFirst();
        assertTrue(facturasRepository.findByOrdenCompraId(orden.getId()).isEmpty());
        String formulario = "/admin/facturas/clientes/orden/" + orden.getId() + "/nueva";
        mvc.perform(get(formulario).with(user(usuario.getNombreUsuario()).roles("JEFE")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Datos de la factura")));
        mvc.perform(post(formulario).with(user(usuario.getNombreUsuario()).roles("JEFE")).with(csrf())
                        .param("nombre", "Luis").param("apellido", "Gomez").param("documento", "99887766")
                        .param("domicilio", "Domicilio comprador").param("correo", "luis@zero.test"))
                .andExpect(status().is3xxRedirection());
        FacturaCliente factura = facturasRepository.findByOrdenCompraId(orden.getId()).orElseThrow();
        assertEquals("Luis", factura.getNombreCliente());
        assertEquals("Gomez", factura.getApellidoCliente());
        assertEquals("Domicilio comprador", factura.getDomicilioCliente());
        assertEquals("luis@zero.test", factura.getCorreoCliente());
        assertTrue(factura.isCorreoPendiente());
        assertFalse(factura.isCorreoEnviado());
        verifyNoInteractions(mercadoPago);
    }

    @Test
    void cambioDeEstadoYAnulacionActualizanLaFactura() {
        Usuario usuario = cliente();
        OrdenCompra primera = comprar(usuario, producto(), TipoPago.BILLETERA_VIRTUAL);
        FacturaCliente factura = facturasRepository.findByOrdenCompraId(primera.getId()).orElseThrow();
        assertEquals(EstadoFactura.SIN_DEFINIR, factura.getEstado());
        flujo.cambiarEstadoAdministracion(primera.getId(), EstadoOrdenCompra.PENDIENTE_ENTREGA);
        assertEquals(EstadoFactura.PAGADA, factura.getEstado());
        assertTrue(factura.isCorreoPendiente());
        OrdenCompra segunda = comprar(usuario, producto(), TipoPago.BILLETERA_VIRTUAL);
        FacturaCliente otra = facturasRepository.findByOrdenCompraId(segunda.getId()).orElseThrow();
        assertTrue(otra.getNumeroFactura() > factura.getNumeroFactura());
        flujo.anularOrdenCliente(segunda.getId(), usuario.getNombreUsuario());
        assertEquals(EstadoFactura.ANULADA, otra.getEstado());
        assertFalse(otra.isCorreoPendiente());
    }
}
