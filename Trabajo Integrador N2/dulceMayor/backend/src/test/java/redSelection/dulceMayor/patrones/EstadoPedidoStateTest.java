package redSelection.dulceMayor.patrones;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import redSelection.dulceMayor.enums.EstadoPedido;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.HistorialEstadoPedido;
import redSelection.dulceMayor.model.Pedido;
import redSelection.dulceMayor.patrones.state.EstadosPedido;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** BK-04: el pedido cambia de estado mediante el patrón State y cada cambio queda en el historial. */
class EstadoPedidoStateTest {

    private Pedido pedido;

    @BeforeEach
    void preparar() {
        pedido = Pedido.builder().fechaHoraCarga(LocalDateTime.now().minusHours(1)).build();
        pedido.registrarAlta("preventista.mza");
    }

    @Test
    @DisplayName("Circuito completo: el historial guarda cada estado con su usuario, en orden")
    void circuitoCompleto() {
        pedido.esperarPago("sistema", "Stock reservado");
        pedido.facturar("admin.mza", null);
        pedido.despachar("transportista.mza", "Ruta del día");
        pedido.entregar("transportista.mza", null);

        assertEquals(EstadoPedido.ENTREGADO, pedido.getEstado());
        List<HistorialEstadoPedido> historial = pedido.getHistorial();
        assertEquals(List.of(EstadoPedido.CARGADO, EstadoPedido.PENDIENTE_PAGO, EstadoPedido.FACTURADO,
                EstadoPedido.EN_REPARTO, EstadoPedido.ENTREGADO),
                historial.stream().map(HistorialEstadoPedido::getEstadoNuevo).toList());
        assertNull(historial.get(0).getEstadoAnterior());
        assertEquals(EstadoPedido.FACTURADO, historial.get(3).getEstadoAnterior());
        assertEquals("transportista.mza", historial.get(3).getUsuario());
        assertEquals("Ruta del día", historial.get(3).getObservacion());
        historial.forEach(h -> assertSame(pedido, h.getPedido()));
        historial.forEach(h -> assertNotNull(h.getFechaHora()));
    }

    @Test
    @DisplayName("Sin stock: CARGADO pasa a PENDIENTE_STOCK, luego espera el pago y recién ahí se factura")
    void pendienteDeStockLuegoFacturado() {
        pedido.quedarPendienteDeStock("sistema", "Falta stock de alfajores");
        assertThrows(ReglaNegocioException.class, () -> pedido.facturar("sistema", null));   // sin pago, no
        pedido.esperarPago("sistema", "Llegó la mercadería");
        pedido.facturar("sistema", "Pago acreditado");

        assertEquals(EstadoPedido.FACTURADO, pedido.getEstado());
        assertEquals(4, pedido.getHistorial().size());
    }

    @Test
    @DisplayName("Un pedido recién cargado no se factura sin pasar por la espera del pago")
    void cargadoNoSeFactura() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class, () -> pedido.facturar("admin.mza", null));
        assertEquals("No se puede pasar un pedido de CARGADO a FACTURADO.", error.getMessage());
    }

    @Test
    @DisplayName("Una transición inválida se rechaza con mensaje claro y no toca el pedido")
    void transicionInvalida() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> pedido.entregar("transportista.mza", null));

        assertEquals("No se puede pasar un pedido de CARGADO a ENTREGADO.", error.getMessage());
        assertEquals(EstadoPedido.CARGADO, pedido.getEstado());
        assertEquals(1, pedido.getHistorial().size());
    }

    @Test
    @DisplayName("Un pedido en reparto ya no se puede anular")
    void enRepartoNoSeAnula() {
        pedido.esperarPago("sistema", null);
        pedido.facturar("admin.mza", null);
        pedido.despachar("transportista.mza", null);

        assertThrows(ReglaNegocioException.class, () -> pedido.anular("admin.mza", null));
        assertEquals(EstadoPedido.EN_REPARTO, pedido.getEstado());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = {"CARGADO", "PENDIENTE_STOCK", "PENDIENTE_PAGO", "FACTURADO"})
    @DisplayName("Se puede anular antes de salir a reparto")
    void anulableAntesDelReparto(EstadoPedido inicial) {
        pedido.setEstado(inicial);
        pedido.anular("admin.mza", "El comercio canceló");
        assertEquals(EstadoPedido.ANULADO, pedido.getEstado());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = {"ENTREGADO", "ANULADO"})
    @DisplayName("Los estados finales no admiten ninguna transición")
    void estadosFinales(EstadoPedido fin) {
        pedido.setEstado(fin);
        for (EstadoPedido destino : EstadoPedido.values()) {
            assertThrows(ReglaNegocioException.class, () -> pedido.avanzarA(destino, "admin.mza", null),
                    fin + " no debería pasar a " + destino);
        }
    }

    @Test
    @DisplayName("avanzarA traduce el destino a la acción correcta")
    void avanzarA() {
        pedido.avanzarA(EstadoPedido.PENDIENTE_PAGO, "admin.mza", "Stock reservado");
        pedido.avanzarA(EstadoPedido.FACTURADO, "admin.mza", "Desde el panel");
        assertEquals(EstadoPedido.FACTURADO, pedido.getEstado());
        assertEquals("Desde el panel", pedido.getHistorial().get(2).getObservacion());
        assertThrows(ReglaNegocioException.class, () -> pedido.avanzarA(EstadoPedido.CARGADO, "admin.mza", null));
    }

    @Test
    @DisplayName("Cada valor del enum tiene su clase State")
    void todosLosEstadosTienenClase() {
        for (EstadoPedido estado : EstadoPedido.values()) {
            assertEquals(estado, EstadosPedido.de(estado).nombre());
        }
    }
}
