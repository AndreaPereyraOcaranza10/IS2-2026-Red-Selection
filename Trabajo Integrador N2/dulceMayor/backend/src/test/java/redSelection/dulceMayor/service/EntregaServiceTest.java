package redSelection.dulceMayor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import redSelection.dulceMayor.dto.EntregaDetalleDTO;
import redSelection.dulceMayor.dto.EntregaResumenDTO;
import redSelection.dulceMayor.dto.PagoRespuestaDTO;
import redSelection.dulceMayor.enums.EstadoFactura;
import redSelection.dulceMayor.enums.EstadoPago;
import redSelection.dulceMayor.enums.EstadoPedido;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.repository.EntregaRepository;
import redSelection.dulceMayor.security.UsuarioActual;
import redSelection.dulceMayor.security.UsuarioAutenticado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** TR-01 / TR-02 / BK-15: historial, detalle y cobro en efectivo del transportista (sin base de datos). */
class EntregaServiceTest {

    private EntregaRepository entregas;
    private FacturaClienteService facturas;
    private PagoService pagos;
    private UsuarioActual usuarioActual;
    private EntregaService servicio;
    private Pedido pedido;
    private Entrega entrega;
    private FacturaCliente factura;

    @BeforeEach
    void preparar() {
        entregas = mock(EntregaRepository.class);
        facturas = mock(FacturaClienteService.class);
        pagos = mock(PagoService.class);
        usuarioActual = mock(UsuarioActual.class);
        servicio = new EntregaService(entregas, facturas, pagos, usuarioActual);

        Direccion direccion = Direccion.builder().calle("San Martín").numeracion("123").localidadNombre("Godoy Cruz").build();
        ResponsableComercio responsable = ResponsableComercio.builder().nombre("Ana").apellido("Pérez").build();
        ClienteMinorista cliente = ClienteMinorista.builder().nombreComercio("Kiosco Ana")
                .responsable(responsable).direccion(direccion).build();
        Producto alfajor = Producto.builder().nombre("Alfajor triple").build();
        alfajor.setId("prod-1");
        pedido = Pedido.builder().fechaHoraCarga(LocalDateTime.now().minusDays(1)).estado(EstadoPedido.EN_REPARTO)
                .cliente(cliente).subtotal(new BigDecimal("1000.00")).descuento(BigDecimal.ZERO)
                .total(new BigDecimal("1000.00")).items(new ArrayList<>()).build();
        pedido.setId("ped-1");
        pedido.getItems().add(PedidoItem.builder().pedido(pedido).producto(alfajor).cantidad(10)
                .precioUnitario(new BigDecimal("100.00")).subtotal(new BigDecimal("1000.00")).build());

        Transportista transportista = Transportista.builder().nombre("Juan").apellido("Gómez")
                .patenteVehiculo("AB123CD").build();
        transportista.setId("trans-1");
        entrega = Entrega.builder().pedido(pedido).transportista(transportista)
                .fechaDespacho(LocalDateTime.now().minusHours(3)).build();

        factura = FacturaCliente.builder().numeroFactura(9L).total(new BigDecimal("1000.00"))
                .fechaFactura(LocalDateTime.now().minusDays(1)).estado(EstadoFactura.EMITIDA).pedido(pedido).build();
        factura.setId("fac-1");

        when(entregas.findByPedidoId("ped-1")).thenReturn(Optional.of(entrega));
        when(facturas.buscarPorPedido("ped-1")).thenReturn(Optional.of(factura));
        when(pagos.pagoVigente(anyString())).thenReturn(Optional.empty());
        actuaComo(Rol.TRANSPORTISTA, "trans-1");
    }

    private void actuaComo(Rol rol, String empleadoId) {
        when(usuarioActual.obtener()).thenReturn(new UsuarioAutenticado("u1", "test", rol, false, empleadoId, 0));
    }

    private static PagoRespuestaDTO pago(TipoPago tipo, EstadoPago estado) {
        return new PagoRespuestaDTO("pg-1", "fac-1", 9L, tipo, estado, new BigDecimal("1000.00"), null, "instrucciones", null);
    }

    // ---------- TR-01 ----------

    @Test @DisplayName("Historial: trae las entregas del transportista con comercio, total y medio de pago")
    void historial() {
        entrega.setFechaEntrega(LocalDateTime.now().minusHours(1));
        when(entregas.findByTransportistaIdAndFechaEntregaBetweenAndEliminadoFalseOrderByFechaEntregaDesc(
                eq("trans-1"), any(), any())).thenReturn(List.of(entrega));
        when(pagos.pagoVigente("fac-1")).thenReturn(Optional.of(pago(TipoPago.EFECTIVO, EstadoPago.CONFIRMADO)));

        List<EntregaResumenDTO> r = servicio.historial("trans-1", null, null);

        assertEquals(1, r.size());
        assertEquals("Kiosco Ana", r.get(0).comercio());
        assertEquals("San Martín 123, Godoy Cruz", r.get(0).direccion());
        assertEquals(TipoPago.EFECTIVO, r.get(0).medioPago());
        assertEquals(new BigDecimal("1000.00"), r.get(0).total());
    }

    @Test @DisplayName("Historial: 'desde' posterior a 'hasta' es un error de regla de negocio")
    void historialRangoInvalido() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.historial("trans-1", LocalDate.now(), LocalDate.now().minusDays(2)));
    }

    @Test @DisplayName("Detalle: devuelve los productos, la factura, el medio de pago y la trazabilidad")
    void detalle() {
        when(pagos.pagoVigente("fac-1")).thenReturn(Optional.of(pago(TipoPago.EFECTIVO, EstadoPago.PENDIENTE)));

        EntregaDetalleDTO d = servicio.detalle("ped-1");

        assertEquals(1, d.items().size());
        assertEquals("Alfajor triple", d.items().get(0).nombre());
        assertEquals(10, d.items().get(0).cantidad());
        assertEquals(9L, d.numeroFactura());
        assertEquals(TipoPago.EFECTIVO, d.medioPago());
        assertEquals(EstadoPago.PENDIENTE, d.estadoPago());
        assertEquals("Juan Gómez", d.transportista());
        assertEquals("AB123CD", d.patenteVehiculo());
    }

    @Test @DisplayName("Detalle sin factura ni pago: los campos de pago vienen en null, no falla")
    void detalleSinFactura() {
        when(facturas.buscarPorPedido("ped-1")).thenReturn(Optional.empty());
        EntregaDetalleDTO d = servicio.detalle("ped-1");
        assertNull(d.numeroFactura());
        assertNull(d.medioPago());
    }

    @Test @DisplayName("Detalle: otro transportista no puede verlo")
    void detalleDeOtro() {
        actuaComo(Rol.TRANSPORTISTA, "trans-2");
        assertThrows(AccessDeniedException.class, () -> servicio.detalle("ped-1"));
    }

    @Test @DisplayName("Detalle: la administración sí puede verlo")
    void detalleAdmin() {
        actuaComo(Rol.ADMINISTRATIVO, null);
        assertDoesNotThrow(() -> servicio.detalle("ped-1"));
    }

    // ---------- TR-02 ----------

    @Test @DisplayName("Cobro: el transportista dueño cobra en efectivo y se delega en PagoService (monto de la factura)")
    void cobra() {
        when(pagos.confirmarFactura("fac-1", TipoPago.EFECTIVO))
                .thenReturn(pago(TipoPago.EFECTIVO, EstadoPago.CONFIRMADO));

        PagoRespuestaDTO r = servicio.cobrarEnEfectivo("ped-1");

        assertEquals(EstadoPago.CONFIRMADO, r.estado());
        verify(pagos).confirmarFactura("fac-1", TipoPago.EFECTIVO);
    }

    @Test @DisplayName("Cobro: se puede cobrar también un pedido ya marcado como entregado")
    void cobraEntregado() {
        pedido.setEstado(EstadoPedido.ENTREGADO);
        when(pagos.confirmarFactura("fac-1", TipoPago.EFECTIVO))
                .thenReturn(pago(TipoPago.EFECTIVO, EstadoPago.CONFIRMADO));
        assertDoesNotThrow(() -> servicio.cobrarEnEfectivo("ped-1"));
    }

    @Test @DisplayName("Cobro: otro transportista no puede cobrar una entrega ajena")
    void cobroAjeno() {
        actuaComo(Rol.TRANSPORTISTA, "trans-2");
        assertThrows(AccessDeniedException.class, () -> servicio.cobrarEnEfectivo("ped-1"));
        verify(pagos, never()).confirmarFactura(anyString(), any());
    }

    @Test @DisplayName("Cobro: administración no cobra por esta vía (usa /api/pagos)")
    void cobroAdmin() {
        actuaComo(Rol.ADMINISTRATIVO, null);
        assertThrows(AccessDeniedException.class, () -> servicio.cobrarEnEfectivo("ped-1"));
    }

    @Test @DisplayName("Cobro: un pedido que todavía no salió a reparto no se cobra")
    void cobroSinReparto() {
        pedido.setEstado(EstadoPedido.FACTURADO);
        assertThrows(ReglaNegocioException.class, () -> servicio.cobrarEnEfectivo("ped-1"));
        verify(pagos, never()).confirmarFactura(anyString(), any());
    }

    @Test @DisplayName("Cobro: sin factura no hay nada que cobrar")
    void cobroSinFactura() {
        when(facturas.buscarPorPedido("ped-1")).thenReturn(Optional.empty());
        assertThrows(ReglaNegocioException.class, () -> servicio.cobrarEnEfectivo("ped-1"));
    }

    @Test @DisplayName("Cobro: si se acordó transferencia, el transportista no la pisa con efectivo")
    void cobroConOtroMedio() {
        when(pagos.pagoVigente("fac-1")).thenReturn(Optional.of(pago(TipoPago.TRANSFERENCIA, EstadoPago.PENDIENTE)));
        assertThrows(ReglaNegocioException.class, () -> servicio.cobrarEnEfectivo("ped-1"));
        verify(pagos, never()).confirmarFactura(anyString(), any());
    }

    @Test @DisplayName("Cobro: repetirlo es idempotente, delega y devuelve el mismo pago confirmado")
    void cobroIdempotente() {
        PagoRespuestaDTO confirmado = pago(TipoPago.EFECTIVO, EstadoPago.CONFIRMADO);
        when(pagos.pagoVigente("fac-1")).thenReturn(Optional.of(confirmado));
        when(pagos.confirmarFactura("fac-1", TipoPago.EFECTIVO)).thenReturn(confirmado);
        assertEquals(servicio.cobrarEnEfectivo("ped-1"), servicio.cobrarEnEfectivo("ped-1"));
    }
}