package redSelection.dulceMayor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.CambioEstadoPedidoDTO;
import redSelection.dulceMayor.dto.PedidoCrearDTO;
import redSelection.dulceMayor.dto.PedidoRespuestaDTO;
import redSelection.dulceMayor.enums.EstadoFactura;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.ClienteMinorista;
import redSelection.dulceMayor.model.FacturaCliente;
import redSelection.dulceMayor.model.Preventista;
import redSelection.dulceMayor.model.Producto;
import redSelection.dulceMayor.repository.ClienteMinoristaRepository;
import redSelection.dulceMayor.repository.PreventistaRepository;
import redSelection.dulceMayor.repository.ProductoRepository;
import redSelection.dulceMayor.security.UsuarioAutenticado;
import redSelection.dulceMayor.service.FacturaClienteService;
import redSelection.dulceMayor.service.PedidoService;
import redSelection.dulceMayor.service.StockService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BK-14: el circuito real pedido -> factura sobre una base vacía (H2 en memoria con los datos de demostración),
 * con todos los componentes de verdad: JPA, patrón State, promociones (Strategy), stock y facturación.
 * Cada test se revierte al terminar (@Transactional), así no se afectan entre sí.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PedidoFacturacionIntegracionTest {

    @Autowired PedidoService pedidos;
    @Autowired FacturaClienteService facturas;
    @Autowired StockService stock;
    @Autowired ProductoRepository productos;
    @Autowired PreventistaRepository preventistas;
    @Autowired ClienteMinoristaRepository clientes;

    private ClienteMinorista comercio;
    private Producto alfajor;
    private Producto gomitas;

    @BeforeEach
    void loguearPreventista() {
        Preventista martin = preventistas.findByEliminadoFalse().stream()
                .filter(p -> p.getUsuario().getNombreUsuario().equals("preventista.mza")).findFirst().orElseThrow();
        comercio = clientes.findByPreventistaAsignadoIdAndEliminadoFalse(martin.getId()).get(0);
        alfajor = productos.findByCodigoIgnoreCase("DM-001").orElseThrow();   // 120 en stock
        gomitas = productos.findByCodigoIgnoreCase("DM-005").orElseThrow();   // 9 en stock
        UsuarioAutenticado usuario = new UsuarioAutenticado(martin.getUsuario().getId(), "preventista.mza",
                Rol.PREVENTISTA, false, martin.getId(), 0);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null,
                List.of(new SimpleGrantedAuthority("ROLE_PREVENTISTA"))));
    }

    @AfterEach
    void desloguear() {
        SecurityContextHolder.clearContext();
    }

    private PedidoRespuestaDTO pedido(Producto producto, int cantidad, String promo) {
        return pedidos.crear(new PedidoCrearDTO(null, null, comercio.getId(), promo,
                List.of(new PedidoCrearDTO.Linea(producto.getId(), cantidad))));
    }

    @Test
    @DisplayName("Con stock: el pedido se factura solo, descuenta el stock y aplica la promoción")
    void conStockSeFacturaSolo() {
        long antes = stock.stockActual(alfajor.getId());

        PedidoRespuestaDTO r = pedido(alfajor, 2, "ALFAJOR15");

        assertEquals("FACTURADO", r.status());
        assertNotNull(r.invoiceId());
        assertEquals(0, new BigDecimal("5520.00").compareTo(r.discount()));    // 15 % de 2 x $18.400
        assertEquals(0, new BigDecimal("31280.00").compareTo(r.total()));
        assertEquals(antes - 2, stock.stockActual(alfajor.getId()));
        assertEquals(List.of("CARGADO", "FACTURADO"), r.trace().events().stream().map(PedidoRespuestaDTO.Evento::state).toList());

        FacturaCliente factura = facturas.obtenerEntidad(r.invoiceId());
        assertEquals(EstadoFactura.EMITIDA, factura.getEstado());
        assertEquals(0, r.total().compareTo(factura.getTotal()));
    }

    @Test
    @DisplayName("Sin stock: el pedido queda PENDIENTE_STOCK, sin factura y sin tocar el stock")
    void sinStockQuedaPendiente() {
        long antes = stock.stockActual(gomitas.getId());

        PedidoRespuestaDTO r = pedido(gomitas, 20, null);

        assertEquals("PENDIENTE_STOCK", r.status());
        assertNull(r.invoiceId());
        assertEquals(antes, stock.stockActual(gomitas.getId()));
        assertTrue(r.trace().events().get(1).note().contains("Gomitas ositos (faltan 11)"));
    }

    @Test
    @DisplayName("Anular un pedido facturado anula la factura y devuelve el stock")
    void anularDevuelveStock() {
        long antes = stock.stockActual(alfajor.getId());
        PedidoRespuestaDTO facturado = pedido(alfajor, 3, null);

        PedidoRespuestaDTO anulado = pedidos.cambiarEstado(facturado.id(), new CambioEstadoPedidoDTO("ANULADO", "El comercio canceló"));

        assertEquals("ANULADO", anulado.status());
        assertEquals(antes, stock.stockActual(alfajor.getId()));
        assertEquals(EstadoFactura.ANULADA, facturas.obtenerEntidad(facturado.invoiceId()).getEstado());
    }

    @Test
    @DisplayName("Una transición inválida se rechaza y el pedido queda como estaba")
    void transicionInvalida() {
        PedidoRespuestaDTO facturado = pedido(alfajor, 1, null);

        assertThrows(ReglaNegocioException.class,
                () -> pedidos.cambiarEstado(facturado.id(), new CambioEstadoPedidoDTO("ENTREGADO", null)));
        assertEquals("FACTURADO", pedidos.obtener(facturado.id()).status());
    }
}
