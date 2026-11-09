package redSelection.dulceMayor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import redSelection.dulceMayor.dto.PromocionCrearDTO;
import redSelection.dulceMayor.dto.PromocionValidadaDTO;
import redSelection.dulceMayor.enums.TipoPromocion;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Producto;
import redSelection.dulceMayor.model.Promocion;
import redSelection.dulceMayor.patrones.strategy.promocion.PromocionPorVolumen;
import redSelection.dulceMayor.patrones.strategy.promocion.PromocionProductoDestacado;
import redSelection.dulceMayor.patrones.strategy.promocion.ReglaPromocion.LineaPromocion;
import redSelection.dulceMayor.repository.PromocionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** BK-06: vigencia del código, descuento por tipo (Strategy) y reglas del alta. */
class PromocionServiceTest {

    private static final LocalDate HOY = LocalDate.now();

    private PromocionRepository repositorio;
    private ProductoService productos;
    private PromocionService servicio;
    private Producto alfajor;

    @BeforeEach
    void preparar() {
        repositorio = mock(PromocionRepository.class);
        productos = mock(ProductoService.class);
        servicio = new PromocionService(repositorio, productos,
                List.of(new PromocionPorVolumen(), new PromocionProductoDestacado()));
        alfajor = Producto.builder().nombre("Alfajor triple chocolate").build();
        alfajor.setId("alfajor");
        when(productos.obtenerEntidad("alfajor")).thenReturn(alfajor);
    }

    private Promocion volumen(String codigo, int minimo, LocalDate desde, LocalDate hasta) {
        return Promocion.builder().codigo(codigo).tipoPromocion(TipoPromocion.VOLUMEN)
                .porcentajeDescuento(new BigDecimal("10")).cantidadMinima(minimo)
                .fechaDesde(desde).fechaHasta(hasta).build();
    }

    private void existe(Promocion p) {
        when(repositorio.findByCodigoIgnoreCaseAndEliminadoFalse(p.getCodigo())).thenReturn(Optional.of(p));
    }

    // ---------- validar el código ----------

    @Test
    @DisplayName("Código vigente: es válido y trae las reglas (se ignoran mayúsculas y espacios)")
    void codigoVigente() {
        existe(volumen("DULCE10", 1, HOY.minusDays(5), null));

        PromocionValidadaDTO r = servicio.validar("  dulce10 ");

        assertTrue(r.valid());
        assertEquals(new BigDecimal("10"), r.discountPercent());
        assertEquals("VOLUMEN", r.type());
    }

    @Test
    @DisplayName("Inexistente, vencido, futuro o desactivado: no es válido y explica por qué")
    void codigosNoValidos() {
        existe(volumen("VENCIDO", 1, HOY.minusDays(30), HOY.minusDays(1)));
        existe(volumen("FUTURO", 1, HOY.plusDays(3), null));
        Promocion apagada = volumen("APAGADA", 1, HOY.minusDays(1), null);
        apagada.setActiva(false);
        existe(apagada);

        assertEquals("El código no existe.", servicio.validar("NOEXISTE").message());
        assertTrue(servicio.validar("VENCIDO").message().contains("venció"));
        assertTrue(servicio.validar("FUTURO").message().contains("empieza a regir"));
        assertTrue(servicio.validar("APAGADA").message().contains("desactivado"));
        assertFalse(servicio.validar("VENCIDO").valid());
    }

    @Test
    @DisplayName("Al cargar el pedido, un código vencido se rechaza con 422")
    void vencidoAlCargarPedido() {
        existe(volumen("VENCIDO", 1, HOY.minusDays(30), HOY.minusDays(1)));
        assertThrows(ReglaNegocioException.class, () -> servicio.vigentePorCodigo("VENCIDO"));
    }

    // ---------- descuento (Strategy) ----------

    @Test
    @DisplayName("Volumen: 10% sobre todo el pedido si alcanza la cantidad mínima")
    void descuentoPorVolumen() {
        Promocion p = volumen("DULCE10", 3, HOY, null);
        List<LineaPromocion> lineas = List.of(
                new LineaPromocion("a", 2, new BigDecimal("1000")),
                new LineaPromocion("b", 1, new BigDecimal("500.55")));

        assertEquals(new BigDecimal("150.06"), servicio.calcularDescuento(p, lineas));   // 10% de 1500,55 redondeado
    }

    @Test
    @DisplayName("Volumen: si no llega a la cantidad mínima, no aplica")
    void volumenSinCantidadMinima() {
        Promocion p = volumen("DULCE10", 5, HOY, null);
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> servicio.calcularDescuento(p,
                List.of(new LineaPromocion("a", 2, new BigDecimal("1000")))));
        assertTrue(e.getMessage().contains("a partir de 5 unidades"));
    }

    @Test
    @DisplayName("Producto destacado: el descuento es solo sobre ese producto")
    void descuentoProductoDestacado() {
        Promocion p = Promocion.builder().codigo("ALFAJOR15").tipoPromocion(TipoPromocion.PRODUCTO_DESTACADO)
                .porcentajeDescuento(new BigDecimal("15")).producto(alfajor).fechaDesde(HOY).build();
        List<LineaPromocion> conAlfajor = List.of(
                new LineaPromocion("alfajor", 2, new BigDecimal("36800")),
                new LineaPromocion("chicles", 1, new BigDecimal("32900")));

        assertEquals(new BigDecimal("5520.00"), servicio.calcularDescuento(p, conAlfajor));  // 15% de 36800
        assertThrows(ReglaNegocioException.class, () -> servicio.calcularDescuento(p,
                List.of(new LineaPromocion("chicles", 1, new BigDecimal("32900")))));
    }

    // ---------- reglas del alta ----------

    @Test
    @DisplayName("Alta: código repetido (aunque la otra esté dada de baja), fechas invertidas o datos faltantes")
    void reglasDelAlta() {
        Promocion borrada = volumen("DULCE10", 1, HOY, null);
        borrada.setEliminado(true);
        when(repositorio.findByCodigoIgnoreCase("DULCE10")).thenReturn(Optional.of(borrada));

        assertThrows(ReglaNegocioException.class, () -> servicio.crear(new PromocionCrearDTO("dulce10",
                TipoPromocion.VOLUMEN, BigDecimal.TEN, 1, null, HOY, null, true)));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(new PromocionCrearDTO("NUEVA",
                TipoPromocion.VOLUMEN, BigDecimal.TEN, 1, null, HOY, HOY.minusDays(1), true)));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(new PromocionCrearDTO("NUEVA",
                TipoPromocion.VOLUMEN, BigDecimal.TEN, null, null, HOY, null, true)));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(new PromocionCrearDTO("NUEVA",
                TipoPromocion.PRODUCTO_DESTACADO, BigDecimal.TEN, null, null, HOY, null, true)));
        verify(repositorio, never()).save(any());
    }

    @Test
    @DisplayName("Alta válida: guarda el código en mayúsculas y solo el dato de su tipo")
    void altaValida() {
        when(repositorio.findByCodigoIgnoreCase("ALFAJOR15")).thenReturn(Optional.empty());
        when(repositorio.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var r = servicio.crear(new PromocionCrearDTO("alfajor15", TipoPromocion.PRODUCTO_DESTACADO,
                new BigDecimal("15"), 99, "alfajor", HOY, null, null));

        assertEquals("ALFAJOR15", r.codigo());
        assertEquals("alfajor", r.productoId());
        assertNull(r.cantidadMinima());   // no es de volumen: la cantidad se descarta
        assertTrue(r.activa());
        assertTrue(r.vigente());
    }
}
