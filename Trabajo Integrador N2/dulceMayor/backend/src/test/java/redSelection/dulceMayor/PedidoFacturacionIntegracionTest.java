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
import redSelection.dulceMayor.dto.PagoCrearDTO;
import redSelection.dulceMayor.dto.PagoRespuestaDTO;
import redSelection.dulceMayor.dto.PedidoCrearDTO;
import redSelection.dulceMayor.dto.PedidoRespuestaDTO;
import redSelection.dulceMayor.enums.EstadoFactura;
import redSelection.dulceMayor.enums.EstadoPago;
import redSelection.dulceMayor.enums.TipoPago;
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
 * BK-14 / BK-15: el circuito real pedido -> reserva de stock -> pago -> factura sobre una base vacía (H2 en memoria con los datos de demostración),
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
        loguear(new UsuarioAutenticado(martin.getUsuario().getId(), "preventista.mza",
                Rol.PREVENTISTA, false, martin.getId(), 0));
    }

    private void loguear(UsuarioAutenticado usuario) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))));
    }

    private void loguearAdministracion() {
        loguear(new UsuarioAutenticado("admin-test", "admin.mza", Rol.ADMINISTRATIVO, false, null, 0));
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
    @DisplayName("Con stock: el pedido reserva el stock, espera el pago sin facturar y aplica la promoción")
    void conStockReservaYEsperaPago() {
        long fisico = stock.stockActual(alfajor.getId());
        long reservado = pedidos.cantidadReservada(alfajor.getId());

        PedidoRespuestaDTO r = pedido(alfajor, 2, "ALFAJOR15");

        assertEquals("PENDIENTE_PAGO", r.status());
        assertNull(r.invoiceId());
        assertEquals(0, new BigDecimal("5520.00").compareTo(r.discount()));    // 15 % de 2 x $18.400
        assertEquals(0, new BigDecimal("31280.00").compareTo(r.total()));
        assertEquals(fisico, stock.stockActual(alfajor.getId()));               // el stock físico no se toca
        assertEquals(reservado + 2, pedidos.cantidadReservada(alfajor.getId()));
        assertEquals(List.of("CARGADO", "PENDIENTE_PAGO"),
                r.trace().events().stream().map(PedidoRespuestaDTO.Evento::state).toList());
    }

    @Test
    @DisplayName("Al registrar el pago se emite la factura (PAGADA), se descuenta el stock y el pedido queda FACTURADO")
    void pagoEmiteFactura() {
        long fisico = stock.stockActual(alfajor.getId());
        PedidoRespuestaDTO creado = pedido(alfajor, 2, null);

        loguearAdministracion();
        PagoRespuestaDTO pago = pedidos.registrarPago(creado.id(), new PagoCrearDTO(TipoPago.EFECTIVO));

        assertEquals(EstadoPago.CONFIRMADO, pago.estado());
        PedidoRespuestaDTO r = pedidos.obtener(creado.id());
        assertEquals("FACTURADO", r.status());
        assertEquals(pago.facturaId(), r.invoiceId());
        assertEquals(fisico - 2, stock.stockActual(alfajor.getId()));
        FacturaCliente factura = facturas.obtenerEntidad(r.invoiceId());
        assertEquals(EstadoFactura.PAGADA, factura.getEstado());
        assertEquals(0, r.total().compareTo(factura.getTotal()));
    }

    @Test
    @DisplayName("Sin stock disponible: el pedido queda PENDIENTE_STOCK, sin factura y sin tocar el stock")
    void sinStockQuedaPendiente() {
        long fisico = stock.stockActual(gomitas.getId());
        long disponible = Math.max(fisico - pedidos.cantidadReservada(gomitas.getId()), 0);

        PedidoRespuestaDTO r = pedido(gomitas, 20, null);

        assertEquals("PENDIENTE_STOCK", r.status());
        assertNull(r.invoiceId());
        assertEquals(fisico, stock.stockActual(gomitas.getId()));
        assertTrue(r.trace().events().get(1).note().contains("Gomitas ositos (faltan " + (20 - disponible) + ")"));
    }

    @Test
    @DisplayName("Anular un pedido que espera el pago libera la reserva; uno ya pagado no se puede anular")
    void anularLiberaReserva() {
        long reservado = pedidos.cantidadReservada(alfajor.getId());
        PedidoRespuestaDTO esperando = pedido(alfajor, 3, null);
        PedidoRespuestaDTO pagado = pedido(alfajor, 1, null);

        loguearAdministracion();
        PedidoRespuestaDTO anulado = pedidos.cambiarEstado(esperando.id(), new CambioEstadoPedidoDTO("ANULADO", "El comercio canceló"));
        assertEquals("ANULADO", anulado.status());

        pedidos.registrarPago(pagado.id(), new PagoCrearDTO(TipoPago.EFECTIVO));
        assertEquals(reservado, pedidos.cantidadReservada(alfajor.getId()));
        assertThrows(ReglaNegocioException.class,
                () -> pedidos.cambiarEstado(pagado.id(), new CambioEstadoPedidoDTO("ANULADO", null)));
    }

    @Test
    @DisplayName("Una transición inválida (o facturar a mano) se rechaza y el pedido queda como estaba")
    void transicionInvalida() {
        PedidoRespuestaDTO creado = pedido(alfajor, 1, null);

        assertThrows(ReglaNegocioException.class,
                () -> pedidos.cambiarEstado(creado.id(), new CambioEstadoPedidoDTO("ENTREGADO", null)));
        assertThrows(ReglaNegocioException.class,
                () -> pedidos.cambiarEstado(creado.id(), new CambioEstadoPedidoDTO("FACTURADO", null)));
        assertEquals("PENDIENTE_PAGO", pedidos.obtener(creado.id()).status());
    }
}
