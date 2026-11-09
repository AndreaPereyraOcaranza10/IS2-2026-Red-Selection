package redSelection.dulceMayor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import redSelection.dulceMayor.enums.*;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.repository.FacturaClienteRepository;
import redSelection.dulceMayor.security.UsuarioAutenticado;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** BK-05: facturación automática con y sin stock, reintento, anulación y quién puede descargar la factura. */
class FacturaClienteServiceTest {

    private FacturaClienteRepository facturas;
    private ProductoService productos;
    private StockService stock;
    private ApplicationEventPublisher eventos;
    private FacturaClienteService servicio;

    private Producto alfajor;
    private Producto chicles;
    private Pedido pedido;

    @BeforeEach
    void preparar() {
        facturas = mock(FacturaClienteRepository.class);
        productos = mock(ProductoService.class);
        stock = mock(StockService.class);
        eventos = mock(ApplicationEventPublisher.class);
        servicio = new FacturaClienteService(facturas, productos, stock, eventos);

        alfajor = producto("alfajor", "Alfajor");
        chicles = producto("chicles", "Chicles");
        pedido = Pedido.builder().fechaHoraCarga(LocalDateTime.now()).build();
        pedido.setId("ped-1");
        linea(alfajor, 2, "100");
        linea(chicles, 1, "50");
        pedido.setSubtotal(new BigDecimal("250"));
        pedido.setTotal(new BigDecimal("250"));
        pedido.registrarAlta("preventista.mza");

        when(facturas.ultimoNumero()).thenReturn(41L);
        when(facturas.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Producto producto(String id, String nombre) {
        Producto p = Producto.builder().nombre(nombre).build();
        p.setId(id);
        when(productos.obtenerConBloqueo(id)).thenReturn(p);
        return p;
    }

    private void linea(Producto p, int cantidad, String precio) {
        BigDecimal unitario = new BigDecimal(precio);
        pedido.getItems().add(PedidoItem.builder().pedido(pedido).producto(p).cantidad(cantidad)
                .precioUnitario(unitario).subtotal(unitario.multiply(BigDecimal.valueOf(cantidad))).build());
    }

    private void hayStock(long alfajores, long chicles) {
        when(stock.stockActual("alfajor")).thenReturn(alfajores);
        when(stock.stockActual("chicles")).thenReturn(chicles);
    }

    private String ultimaObservacion() {
        return pedido.getHistorial().get(pedido.getHistorial().size() - 1).getObservacion();
    }

    // ---------- facturación automática ----------

    @Test
    @DisplayName("Con stock: emite la factura correlativa, descuenta cada línea y el pedido queda FACTURADO")
    void conStock() {
        hayStock(10, 10);

        FacturaCliente factura = servicio.facturarAlCargar(pedido, "preventista.mza").orElseThrow();

        assertEquals(42L, factura.getNumeroFactura());
        assertEquals(EstadoFactura.EMITIDA, factura.getEstado());
        assertEquals(new BigDecimal("250"), factura.getTotal());
        assertEquals(2, factura.getDetalles().size());
        assertEquals(EstadoPedido.FACTURADO, pedido.getEstado());
        assertEquals(EstadoStockPedido.CONFIRMADO, pedido.getEstadoStock());
        assertEquals("Factura N° 42", ultimaObservacion());
        verify(stock).registrar(eq(alfajor), eq(TipoMovimientoStock.VENTA), eq(-2), any(DetalleFactura.class));
        verify(stock).registrar(eq(chicles), eq(TipoMovimientoStock.VENTA), eq(-1), any(DetalleFactura.class));
        verify(eventos).publishEvent(any(FacturaEmitidaEvento.class));   // BK-08: dispara el correo
    }

    @Test
    @DisplayName("Sin stock: no factura, no toca el stock y el pedido queda PENDIENTE_STOCK diciendo qué falta")
    void sinStock() {
        hayStock(10, 0);

        assertTrue(servicio.facturarAlCargar(pedido, "preventista.mza").isEmpty());

        assertEquals(EstadoPedido.PENDIENTE_STOCK, pedido.getEstado());
        assertEquals(EstadoStockPedido.INSUFICIENTE, pedido.getEstadoStock());
        assertEquals("Falta stock de Chicles (faltan 1)", ultimaObservacion());
        verify(facturas, never()).save(any());
        verify(stock, never()).registrar(any(), any(), anyInt(), any());
        verifyNoInteractions(eventos);   // sin factura no hay correo
    }

    @Test
    @DisplayName("Si un producto aparece en dos líneas, se suman las cantidades para controlar el stock")
    void lineasRepetidas() {
        linea(alfajor, 2, "100");   // ahora pide 4 alfajores en total
        hayStock(3, 10);

        assertTrue(servicio.facturarAlCargar(pedido, "preventista.mza").isEmpty());
        assertEquals("Falta stock de Alfajor (faltan 1)", ultimaObservacion());
    }

    // ---------- facturación pedida por administración ----------

    @Test
    @DisplayName("Un pedido que esperaba stock se factura cuando llega la mercadería; si todavía falta, se rechaza")
    void reintentoDePendiente() {
        hayStock(10, 0);
        servicio.facturarAlCargar(pedido, "sistema");

        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> servicio.facturarPedido(pedido, "admin.mza"));
        assertEquals("Todavía falta stock de Chicles (faltan 1).", e.getMessage());
        assertEquals(EstadoPedido.PENDIENTE_STOCK, pedido.getEstado());

        hayStock(10, 5);
        FacturaCliente factura = servicio.facturarPedido(pedido, "admin.mza");
        assertEquals(42L, factura.getNumeroFactura());
        assertEquals(EstadoPedido.FACTURADO, pedido.getEstado());
    }

    @Test
    @DisplayName("Un pedido entregado no se factura: se rechaza antes de tocar productos o stock")
    void estadoQueNoSeFactura() {
        pedido.setEstado(EstadoPedido.ENTREGADO);

        assertThrows(ReglaNegocioException.class, () -> servicio.facturarPedido(pedido, "admin.mza"));
        verifyNoInteractions(productos, stock);
    }

    @Test
    @DisplayName("Facturar dos veces el mismo pedido devuelve la factura que ya tenía")
    void idempotente() {
        FacturaCliente existente = FacturaCliente.builder().numeroFactura(7L).build();
        when(facturas.findByPedidoId("ped-1")).thenReturn(Optional.of(existente));

        assertSame(existente, servicio.facturarPedido(pedido, "admin.mza"));
        verify(facturas, never()).save(any());
    }

    // ---------- anulación ----------

    private FacturaCliente facturaEmitida() {
        FacturaCliente f = FacturaCliente.builder().numeroFactura(42L).pedido(pedido).detalles(new ArrayList<>()).build();
        f.getDetalles().add(DetalleFactura.builder().factura(f).producto(alfajor).cantidad(2).build());
        when(facturas.findByPedidoId("ped-1")).thenReturn(Optional.of(f));
        return f;
    }

    @Test
    @DisplayName("Anular un pedido facturado anula la factura y devuelve el stock")
    void anulaFacturaYDevuelveStock() {
        FacturaCliente f = facturaEmitida();

        servicio.anularFacturaDe(pedido);

        assertEquals(EstadoFactura.ANULADA, f.getEstado());
        verify(stock).registrar(eq(alfajor), eq(TipoMovimientoStock.REVERSION), eq(2), any(DetalleFactura.class));
    }

    @Test
    @DisplayName("Con la factura pagada, el pedido no se puede anular")
    void noAnulaFacturaPagada() {
        FacturaCliente f = facturaEmitida();
        f.setEstado(EstadoFactura.PAGADA);

        assertThrows(ReglaNegocioException.class, () -> servicio.anularFacturaDe(pedido));
        verify(stock, never()).registrar(any(), any(), anyInt(), any());
    }

    // ---------- descarga ----------

    @Test
    @DisplayName("Descarga: administración y el preventista del pedido sí; otro preventista, no (404)")
    void quienPuedeDescargar() {
        Preventista martin = Preventista.builder().build();
        martin.setId("prev-1");
        pedido.setPreventista(martin);
        pedido.setCliente(ClienteMinorista.builder().build());
        FacturaCliente f = FacturaCliente.builder().pedido(pedido).build();
        when(facturas.findByIdAndEliminadoFalse("fac-1")).thenReturn(Optional.of(f));

        assertSame(f, servicio.obtenerParaDescarga("fac-1", usuario(Rol.ADMINISTRATIVO, null)));
        assertSame(f, servicio.obtenerParaDescarga("fac-1", usuario(Rol.PREVENTISTA, "prev-1")));
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.obtenerParaDescarga("fac-1", usuario(Rol.PREVENTISTA, "prev-2")));
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.obtenerParaDescarga("fac-1", usuario(Rol.TRANSPORTISTA, "t-1")));
    }

    private UsuarioAutenticado usuario(Rol rol, String empleadoId) {
        return new UsuarioAutenticado("u", "usuario", rol, false, empleadoId, 0);
    }
}
