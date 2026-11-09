package redSelection.dulceMayor.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;
import redSelection.dulceMayor.controller.PortalOrderController.OrderLineRequest;
import redSelection.dulceMayor.controller.PortalOrderController.OrderRequest;
import redSelection.dulceMayor.controller.PortalOrderController.OrderResponse;
import redSelection.dulceMayor.enums.EstadoPedido;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.repository.*;
import redSelection.dulceMayor.security.UsuarioActual;
import redSelection.dulceMayor.security.UsuarioAutenticado;
import redSelection.dulceMayor.service.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** PV-01: el preventista carga pedidos solo para SUS puntos de venta y ve solo sus pedidos. */
class PedidosPreventistaTest {

    private PedidoRepository pedidos;
    private ClienteMinoristaRepository clientes;
    private PreventistaRepository preventistas;
    private RutaService rutas;
    private PromocionService promociones;
    private PortalOrderController controller;

    private Preventista yo;
    private Preventista otro;
    private ClienteMinorista miKiosco;
    private ClienteMinorista kioscoAjeno;
    private Authentication auth;

    @BeforeEach
    void preparar() {
        pedidos = mock(PedidoRepository.class);
        clientes = mock(ClienteMinoristaRepository.class);
        preventistas = mock(PreventistaRepository.class);
        rutas = mock(RutaService.class);
        ProductoService productos = mock(ProductoService.class);
        PrecioService precios = mock(PrecioService.class);
        StockService stock = mock(StockService.class);
        FacturaClienteRepository facturas = mock(FacturaClienteRepository.class);
        UsuarioActual usuarioActual = mock(UsuarioActual.class);
        promociones = mock(PromocionService.class);
        controller = new PortalOrderController(pedidos, clientes, mock(ResponsableComercioRepository.class),
                preventistas, productos, precios, stock, facturas, usuarioActual, rutas, promociones);

        yo = Preventista.builder().build();
        yo.setId("prev-1");
        otro = Preventista.builder().build();
        otro.setId("prev-2");
        miKiosco = ClienteMinorista.builder().nombreComercio("Kiosco El Sol").preventistaAsignado(yo).build();
        miKiosco.setId("cli-1");
        kioscoAjeno = ClienteMinorista.builder().nombreComercio("Kiosco Ajeno").preventistaAsignado(otro).build();
        kioscoAjeno.setId("cli-2");

        Producto alfajor = Producto.builder().nombre("Alfajor").build();
        alfajor.setId("prod-1");
        when(productos.obtenerConBloqueo("prod-1")).thenReturn(alfajor);
        when(precios.precioVigenteObligatorio(any(), any())).thenReturn(new BigDecimal("1000"));
        when(stock.stockActual("prod-1")).thenReturn(50L);
        when(facturas.findByPedidoId(any())).thenReturn(Optional.empty());
        when(usuarioActual.nombreOSistema()).thenReturn("preventista.mza");
        when(preventistas.findByIdAndEliminadoFalse("prev-1")).thenReturn(Optional.of(yo));
        when(clientes.findByIdAndEliminadoFalse("cli-1")).thenReturn(Optional.of(miKiosco));
        when(clientes.findByIdAndEliminadoFalse("cli-2")).thenReturn(Optional.of(kioscoAjeno));
        when(pedidos.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UsuarioAutenticado principal = new UsuarioAutenticado("u-1", "preventista.mza", Rol.PREVENTISTA, false, "prev-1", 0);
        auth = new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_PREVENTISTA")));
    }

    private OrderRequest pedidoPara(String puntoId) {
        return pedidoPara(puntoId, null);
    }

    private OrderRequest pedidoPara(String puntoId, String promoCode) {
        return new OrderRequest(null, null, puntoId, promoCode, List.of(new OrderLineRequest("prod-1", 3)));
    }

    @Test
    @DisplayName("BK-06: un código válido reduce el total y queda guardado en el pedido")
    void codigoValidoReduceElTotal() {
        Promocion dulce10 = Promocion.builder().codigo("DULCE10").build();
        when(promociones.vigentePorCodigo("dulce10")).thenReturn(dulce10);
        when(promociones.calcularDescuento(eq(dulce10), any())).thenReturn(new BigDecimal("300.00"));

        OrderResponse r = controller.create(pedidoPara("cli-1", "dulce10"), auth);

        assertEquals(new BigDecimal("300.00"), r.discount());
        assertEquals(new BigDecimal("2700.00"), r.total());
        verify(pedidos).save(argThat(p -> p.getPromocion() == dulce10 && p.getSubtotal().compareTo(new BigDecimal("3000")) == 0));
    }

    @Test
    @DisplayName("BK-06: un código vencido o inexistente rechaza el pedido y no se guarda nada")
    void codigoInvalidoRechazaElPedido() {
        when(promociones.vigentePorCodigo("VIEJO"))
                .thenThrow(new redSelection.dulceMayor.exception.ReglaNegocioException("El código VIEJO venció."));

        assertThrows(redSelection.dulceMayor.exception.ReglaNegocioException.class,
                () -> controller.create(pedidoPara("cli-1", "VIEJO"), auth));
        verify(pedidos, never()).save(any());
        verifyNoInteractions(rutas);
    }

    @Test
    @DisplayName("Carga un pedido para su comercio: queda a su nombre, CARGADO y con la visita registrada")
    void cargaPedidoPropio() {
        OrderResponse r = controller.create(pedidoPara("cli-1"), auth);

        assertEquals("CARGADO", r.status());
        assertEquals(new BigDecimal("3000"), r.total());
        assertEquals("cli-1", r.pointOfSale().id());
        assertEquals("Kiosco El Sol", r.pointOfSale().name());
        assertEquals(1, r.trace().events().size());
        assertEquals("preventista.mza", r.trace().events().get(0).user());
        verify(pedidos).save(argThat(p -> p.getPreventista() == yo && p.getCliente() == miKiosco));
        verify(rutas).registrarVisitaPorPedido("prev-1", "cli-1");
    }

    @Test
    @DisplayName("No puede cargar pedidos en un comercio de otro preventista")
    void noCargaEnComercioAjeno() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> controller.create(pedidoPara("cli-2"), auth));

        assertEquals(404, e.getStatusCode().value());
        verify(pedidos, never()).save(any());
        verifyNoInteractions(rutas);
    }

    @Test
    @DisplayName("La lista trae solo los pedidos del preventista logueado")
    void listaSoloLosPropios() {
        Pedido mio = pedido(miKiosco, yo);
        when(pedidos.findByPreventistaIdOrderByFechaHoraCargaDesc("prev-1")).thenReturn(List.of(mio));

        List<OrderResponse> lista = controller.list(auth);

        assertEquals(1, lista.size());
        verify(pedidos, never()).findByEliminadoFalse();
    }

    @Test
    @DisplayName("El detalle de un pedido ajeno responde 404 (no confirma que exista)")
    void detalleAjeno() {
        when(pedidos.findByIdAndEliminadoFalse("ped-ajeno")).thenReturn(Optional.of(pedido(kioscoAjeno, otro)));
        when(pedidos.findByIdAndEliminadoFalse("ped-mio")).thenReturn(Optional.of(pedido(miKiosco, yo)));

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> controller.get("ped-ajeno", auth));
        assertEquals(404, e.getStatusCode().value());
        assertEquals("CARGADO", controller.get("ped-mio", auth).status());
    }

    private Pedido pedido(ClienteMinorista cliente, Preventista preventista) {
        return Pedido.builder().fechaHoraCarga(LocalDateTime.now()).estado(EstadoPedido.CARGADO)
                .cliente(cliente).preventista(preventista).total(BigDecimal.TEN).build();
    }
}
