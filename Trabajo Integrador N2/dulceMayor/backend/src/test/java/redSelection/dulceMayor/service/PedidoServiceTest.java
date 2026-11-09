package redSelection.dulceMayor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import redSelection.dulceMayor.dto.PedidoCrearDTO;
import redSelection.dulceMayor.dto.PedidoRespuestaDTO;
import redSelection.dulceMayor.enums.EstadoPedido;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.repository.ClienteMinoristaRepository;
import redSelection.dulceMayor.repository.PedidoRepository;
import redSelection.dulceMayor.repository.PreventistaRepository;
import redSelection.dulceMayor.repository.ResponsableComercioRepository;
import redSelection.dulceMayor.security.UsuarioActual;
import redSelection.dulceMayor.security.UsuarioAutenticado;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pedidos (PV-01, BK-05, BK-06): el preventista carga pedidos solo para SUS puntos de venta y ve solo los suyos;
 * el pedido se intenta facturar solo, aplica el código de promoción y su trazabilidad muestra al transportista.
 */
class PedidoServiceTest {

    private PedidoRepository pedidos;
    private ClienteMinoristaRepository clientes;
    private PreventistaRepository preventistas;
    private RutaService rutas;
    private PromocionService promociones;
    private FacturaClienteService facturacion;
    private EntregaService entregas;
    private UsuarioActual usuarioActual;
    private PedidoService servicio;

    private Preventista yo;
    private Preventista otro;
    private ClienteMinorista miKiosco;
    private ClienteMinorista kioscoAjeno;

    @BeforeEach
    void preparar() {
        pedidos = mock(PedidoRepository.class);
        clientes = mock(ClienteMinoristaRepository.class);
        preventistas = mock(PreventistaRepository.class);
        rutas = mock(RutaService.class);
        promociones = mock(PromocionService.class);
        facturacion = mock(FacturaClienteService.class);
        entregas = mock(EntregaService.class);
        usuarioActual = mock(UsuarioActual.class);
        ProductoService productos = mock(ProductoService.class);
        PrecioService precios = mock(PrecioService.class);
        servicio = new PedidoService(pedidos, clientes, mock(ResponsableComercioRepository.class), preventistas,
                productos, precios, promociones, facturacion, rutas, entregas, usuarioActual);

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
        when(productos.obtenerEntidad("prod-1")).thenReturn(alfajor);
        when(precios.precioVigenteObligatorio(any(), any())).thenReturn(new BigDecimal("1000"));
        when(facturacion.buscarPorPedido(any())).thenReturn(Optional.empty());
        when(entregas.buscarPorPedido(any())).thenReturn(Optional.empty());
        when(preventistas.findByIdAndEliminadoFalse("prev-1")).thenReturn(Optional.of(yo));
        when(clientes.findByIdAndEliminadoFalse("cli-1")).thenReturn(Optional.of(miKiosco));
        when(clientes.findByIdAndEliminadoFalse("cli-2")).thenReturn(Optional.of(kioscoAjeno));
        when(pedidos.save(any())).thenAnswer(inv -> inv.getArgument(0));
        logueado(Rol.PREVENTISTA, "prev-1");
    }

    private void logueado(Rol rol, String empleadoId) {
        UsuarioAutenticado u = new UsuarioAutenticado("u-1", "preventista.mza", rol, false, empleadoId, 0);
        when(usuarioActual.obtener()).thenReturn(u);
        when(usuarioActual.nombreOSistema()).thenReturn(u.getNombreUsuario());
    }

    private PedidoCrearDTO pedidoPara(String puntoId, String promoCode) {
        return new PedidoCrearDTO(null, null, puntoId, promoCode, List.of(new PedidoCrearDTO.Linea("prod-1", 3)));
    }

    private Pedido pedido(ClienteMinorista cliente, Preventista preventista) {
        return Pedido.builder().fechaHoraCarga(LocalDateTime.now()).estado(EstadoPedido.CARGADO)
                .cliente(cliente).preventista(preventista).total(BigDecimal.TEN).build();
    }

    // ---------- alta ----------

    @Test
    @DisplayName("Carga un pedido para su comercio: queda a su nombre, se intenta facturar y cuenta como visita")
    void cargaPedidoPropio() {
        PedidoRespuestaDTO r = servicio.crear(pedidoPara("cli-1", null));

        assertEquals("CARGADO", r.status());   // la facturación está simulada en este test
        assertEquals(new BigDecimal("3000"), r.total());
        assertEquals("cli-1", r.pointOfSale().id());
        assertEquals("preventista.mza", r.trace().events().get(0).user());
        verify(pedidos).save(argThat(p -> p.getPreventista() == yo && p.getCliente() == miKiosco));
        verify(facturacion).facturarAlCargar(any(), eq("preventista.mza"));
        verify(rutas).registrarVisitaPorPedido("prev-1", "cli-1");
    }

    @Test
    @DisplayName("No puede cargar pedidos en un comercio de otro preventista (404)")
    void noCargaEnComercioAjeno() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.crear(pedidoPara("cli-2", null)));
        verify(pedidos, never()).save(any());
        verifyNoInteractions(rutas, facturacion);
    }

    @Test
    @DisplayName("BK-06: un código válido reduce el total y queda guardado en el pedido")
    void codigoValidoReduceElTotal() {
        Promocion dulce10 = Promocion.builder().codigo("DULCE10").build();
        when(promociones.vigentePorCodigo("dulce10")).thenReturn(dulce10);
        when(promociones.calcularDescuento(eq(dulce10), any())).thenReturn(new BigDecimal("300.00"));

        PedidoRespuestaDTO r = servicio.crear(pedidoPara("cli-1", "dulce10"));

        assertEquals(new BigDecimal("300.00"), r.discount());
        assertEquals(new BigDecimal("2700.00"), r.total());
        verify(pedidos).save(argThat(p -> p.getPromocion() == dulce10));
    }

    @Test
    @DisplayName("BK-06: un código vencido o inexistente rechaza el pedido y no se guarda nada")
    void codigoInvalidoRechazaElPedido() {
        when(promociones.vigentePorCodigo("VIEJO")).thenThrow(new ReglaNegocioException("El código VIEJO venció."));

        assertThrows(ReglaNegocioException.class, () -> servicio.crear(pedidoPara("cli-1", "VIEJO")));
        verify(pedidos, never()).save(any());
        verifyNoInteractions(rutas, facturacion);
    }

    // ---------- lecturas ----------

    @Test
    @DisplayName("La lista trae solo los pedidos del preventista logueado")
    void listaSoloLosPropios() {
        when(pedidos.findByPreventistaIdOrderByFechaHoraCargaDesc("prev-1")).thenReturn(List.of(pedido(miKiosco, yo)));

        assertEquals(1, servicio.listar().size());
        verify(pedidos, never()).findByEliminadoFalse();
    }

    @Test
    @DisplayName("El detalle de un pedido ajeno responde 404 (no confirma que exista)")
    void detalleAjeno() {
        when(pedidos.findByIdAndEliminadoFalse("ped-ajeno")).thenReturn(Optional.of(pedido(kioscoAjeno, otro)));
        when(pedidos.findByIdAndEliminadoFalse("ped-mio")).thenReturn(Optional.of(pedido(miKiosco, yo)));

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener("ped-ajeno"));
        assertEquals("CARGADO", servicio.obtener("ped-mio").status());
    }

    @Test
    @DisplayName("Trazabilidad: muestra al transportista cuando el pedido salió a reparto")
    void trazabilidadConTransportista() {
        Pedido enReparto = pedido(miKiosco, yo);
        enReparto.setId("ped-1");
        Transportista diego = Transportista.builder().nombre("Diego").apellido("Rojas").patenteVehiculo("AA123BB").build();
        when(pedidos.findByIdAndEliminadoFalse("ped-1")).thenReturn(Optional.of(enReparto));
        when(entregas.buscarPorPedido("ped-1")).thenReturn(Optional.of(Entrega.builder().transportista(diego).build()));

        PedidoRespuestaDTO.Transportista carrier = servicio.obtener("ped-1").trace().carrier();

        assertEquals("Diego Rojas", carrier.name());
        assertEquals("Patente AA123BB", carrier.vehicle());
    }

    // ---------- modificación y baja ----------

    @Test
    @DisplayName("Un pedido no se modifica ni se borra: se anula con un cambio de estado")
    void noSeModificaNiSeBorra() {
        when(pedidos.findByIdAndEliminadoFalse("ped-mio")).thenReturn(Optional.of(pedido(miKiosco, yo)));

        assertThrows(ReglaNegocioException.class, () -> servicio.actualizar("ped-mio", pedidoPara("cli-1", null)));
        assertThrows(ReglaNegocioException.class, () -> servicio.eliminar("ped-mio"));
        verify(pedidos, never()).save(any());
    }
}
