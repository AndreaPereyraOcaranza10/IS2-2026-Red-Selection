package redSelection.dulceMayor.patrones;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import redSelection.dulceMayor.enums.EstadoPago;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.enums.TipoEmpresa;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.DulceMayor;
import redSelection.dulceMayor.model.Pago;
import redSelection.dulceMayor.model.Pedido;
import redSelection.dulceMayor.patrones.adapter.PasarelaPago;
import redSelection.dulceMayor.patrones.adapter.PasarelaSimulada;
import redSelection.dulceMayor.patrones.factory.*;
import redSelection.dulceMayor.service.DulceMayorService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** BK-07 / BK-15 / IN-04: el Factory elige el medio correcto y cada medio crea el Pago del pedido. */
class MedioPagoFactoryTest {

    private DulceMayorService empresas;
    private PasarelaPago pasarela;
    private MedioPagoFactory factory;

    @BeforeEach
    void preparar() {
        empresas = mock(DulceMayorService.class);
        DulceMayor sede = DulceMayor.builder().tipoEmpresa(TipoEmpresa.SEDE_CENTRAL)
                .aliasTransferencia("ALIAS.PRUEBA").build();
        when(empresas.sedeCentral()).thenReturn(Optional.of(sede));
        pasarela = new PasarelaSimulada();
        factory = new MedioPagoFactory(List.of(
                new EfectivoMedioPago(),
                new TransferenciaMedioPago(empresas),
                new BilleteraVirtualMedioPago(pasarela)));
    }

    private Pedido pedido() {
        Pedido p = Pedido.builder().total(new BigDecimal("1500.00")).build();
        p.setId("ped-1");
        return p;
    }

    @Test @DisplayName("Devuelve el medio que corresponde a cada tipo")
    void eligeMedio() {
        for (TipoPago tipo : TipoPago.values()) {
            assertEquals(tipo, factory.obtener(tipo).tipo());
        }
    }

    @Test @DisplayName("El Pago nace PENDIENTE, ligado al pedido y con su monto")
    void creaPagoPendiente() {
        Pedido pedido = pedido();
        Pago pago = factory.crear(TipoPago.EFECTIVO, pedido);
        assertEquals(EstadoPago.PENDIENTE, pago.getEstado());
        assertEquals(new BigDecimal("1500.00"), pago.getMonto());
        assertSame(pedido, pago.getPedido());
        assertNull(pago.getCodigoQr());
    }

    @Test @DisplayName("Transferencia muestra el alias cargado en la empresa")
    void transferenciaMuestraAlias() {
        Pago pago = factory.crear(TipoPago.TRANSFERENCIA, pedido());
        assertTrue(factory.obtener(TipoPago.TRANSFERENCIA).instrucciones(pago).contains("ALIAS.PRUEBA"));
    }

    @Test @DisplayName("Sin alias cargado, la transferencia no se puede iniciar")
    void transferenciaSinAlias() {
        when(empresas.sedeCentral()).thenReturn(Optional.empty());
        assertThrows(ReglaNegocioException.class, () -> factory.crear(TipoPago.TRANSFERENCIA, pedido()));
    }

    @Test @DisplayName("Los tres medios están disponibles")
    void disponibilidad() {
        for (TipoPago tipo : TipoPago.values()) {
            assertTrue(factory.obtener(tipo).disponible(), tipo.name());
        }
    }

    @Test @DisplayName("Billetera virtual pide la orden a la pasarela y guarda su referencia y el QR")
    void billeteraGeneraQr() {
        MedioPago medio = factory.obtener(TipoPago.BILLETERA_VIRTUAL);
        Pago pago = medio.crearPago(pedido());
        pago.setId("p-123");
        medio.alGuardar(pago);
        assertNotNull(pago.getReferenciaExterna());
        assertTrue(pago.getCodigoQr().contains(pago.getReferenciaExterna()));
        assertEquals(PasarelaPago.EstadoOrden.PENDIENTE, pasarela.consultar(pago.getReferenciaExterna()));
    }

    @Test @DisplayName("Transferencia: solo la administración confirma (el cliente y el preventista no)")
    void quienConfirmaTransferencia() {
        MedioPago medio = factory.obtener(TipoPago.TRANSFERENCIA);
        assertTrue(medio.puedeConfirmar(Rol.ADMINISTRATIVO));
        assertTrue(medio.puedeConfirmar(Rol.JEFE_ADMINISTRATIVO));
        assertFalse(medio.puedeConfirmar(Rol.CLIENTE));
        assertFalse(medio.puedeConfirmar(Rol.PREVENTISTA));
    }

    @Test @DisplayName("Efectivo lo confirma también el transportista; billetera virtual no la confirma nadie a mano")
    void quienConfirmaOtrosMedios() {
        assertTrue(factory.obtener(TipoPago.EFECTIVO).puedeConfirmar(Rol.TRANSPORTISTA));
        assertTrue(factory.obtener(TipoPago.EFECTIVO).puedeConfirmar(Rol.ADMINISTRATIVO));
        assertFalse(factory.obtener(TipoPago.BILLETERA_VIRTUAL).puedeConfirmar(Rol.ADMINISTRATIVO));
        assertFalse(factory.obtener(TipoPago.BILLETERA_VIRTUAL).puedeConfirmar(Rol.JEFE_ADMINISTRATIVO));
    }

    @Test @DisplayName("Si falta un medio para algún TipoPago, falla al construir (no en producción)")
    void faltaUnMedio() {
        assertThrows(IllegalStateException.class, () -> new MedioPagoFactory(List.of(new EfectivoMedioPago())));
    }

    @Test @DisplayName("Dos medios para el mismo tipo también es un error de configuración")
    void medioDuplicado() {
        assertThrows(IllegalStateException.class, () -> new MedioPagoFactory(List.of(
                new EfectivoMedioPago(), new EfectivoMedioPago(),
                new TransferenciaMedioPago(empresas), new BilleteraVirtualMedioPago(pasarela))));
    }
}
