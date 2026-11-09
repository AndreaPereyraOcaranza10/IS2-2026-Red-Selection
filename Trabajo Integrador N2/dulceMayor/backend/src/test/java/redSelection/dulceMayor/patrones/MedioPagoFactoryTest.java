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
import redSelection.dulceMayor.model.FacturaCliente;
import redSelection.dulceMayor.model.Pago;
import redSelection.dulceMayor.patrones.factory.*;
import redSelection.dulceMayor.repository.DulceMayorRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** BK-07 / BK-15: el Factory elige el medio correcto y cada medio crea su Pago. */
class MedioPagoFactoryTest {

    private DulceMayorRepository empresas;
    private MedioPagoFactory factory;

    @BeforeEach
    void preparar() {
        empresas = mock(DulceMayorRepository.class);
        DulceMayor sede = DulceMayor.builder().tipoEmpresa(TipoEmpresa.SEDE_CENTRAL)
                .aliasTransferencia("ALIAS.PRUEBA").build();
        when(empresas.findByEliminadoFalse()).thenReturn(List.of(sede));
        factory = new MedioPagoFactory(List.of(
                new EfectivoMedioPago(),
                new TransferenciaMedioPago(empresas),
                new BilleteraVirtualMedioPago("http://app/pagos/")));
    }

    private FacturaCliente factura() {
        FacturaCliente f = FacturaCliente.builder().numeroFactura(1L).total(new BigDecimal("1500.00")).build();
        f.setId("f1");
        return f;
    }

    @Test @DisplayName("Devuelve el medio que corresponde a cada tipo")
    void eligeMedio() {
        for (TipoPago tipo : TipoPago.values()) {
            assertEquals(tipo, factory.obtener(tipo).tipo());
        }
    }

    @Test @DisplayName("El Pago nace PENDIENTE y con el monto de la factura")
    void creaPagoPendiente() {
        Pago pago = factory.crear(TipoPago.EFECTIVO, factura());
        assertEquals(EstadoPago.PENDIENTE, pago.getEstado());
        assertEquals(new BigDecimal("1500.00"), pago.getMonto());
        assertNull(pago.getCodigoQr());
    }

    @Test @DisplayName("Transferencia muestra el alias cargado en la empresa")
    void transferenciaMuestraAlias() {
        Pago pago = factory.crear(TipoPago.TRANSFERENCIA, factura());
        assertTrue(factory.obtener(TipoPago.TRANSFERENCIA).instrucciones(pago).contains("ALIAS.PRUEBA"));
    }

    @Test @DisplayName("Sin alias cargado, la transferencia no se puede iniciar")
    void transferenciaSinAlias() {
        when(empresas.findByEliminadoFalse()).thenReturn(List.of());
        assertThrows(ReglaNegocioException.class, () -> factory.crear(TipoPago.TRANSFERENCIA, factura()));
    }

    @Test @DisplayName("Efectivo y transferencia están disponibles; billetera virtual todavía no")
    void disponibilidad() {
        assertTrue(factory.obtener(TipoPago.EFECTIVO).disponible());
        assertTrue(factory.obtener(TipoPago.TRANSFERENCIA).disponible());
        assertFalse(factory.obtener(TipoPago.BILLETERA_VIRTUAL).disponible());
    }

    @Test @DisplayName("Billetera virtual arma el QR con el id del pago (queda listo para cuando se habilite)")
    void billeteraGeneraQr() {
        MedioPago medio = factory.obtener(TipoPago.BILLETERA_VIRTUAL);
        Pago pago = medio.crearPago(factura());
        pago.setId("p-123");
        medio.alGuardar(pago);
        assertEquals("http://app/pagos/p-123", pago.getCodigoQr());
    }

    @Test @DisplayName("Solo la administración confirma (el cliente y el preventista no)")
    void quienConfirma() {
        MedioPago medio = factory.obtener(TipoPago.TRANSFERENCIA);
        assertTrue(medio.puedeConfirmar(Rol.ADMINISTRATIVO));
        assertTrue(medio.puedeConfirmar(Rol.JEFE_ADMINISTRATIVO));
        assertFalse(medio.puedeConfirmar(Rol.CLIENTE));
        assertFalse(medio.puedeConfirmar(Rol.PREVENTISTA));
    }

    @Test @DisplayName("Si falta un medio para algún TipoPago, falla al construir (no en producción)")
    void faltaUnMedio() {
        assertThrows(IllegalStateException.class, () -> new MedioPagoFactory(List.of(new EfectivoMedioPago())));
    }

    @Test @DisplayName("Dos medios para el mismo tipo también es un error de configuración")
    void medioDuplicado() {
        assertThrows(IllegalStateException.class, () -> new MedioPagoFactory(List.of(
                new EfectivoMedioPago(), new EfectivoMedioPago(),
                new TransferenciaMedioPago(empresas), new BilleteraVirtualMedioPago("http://x"))));
    }
}
