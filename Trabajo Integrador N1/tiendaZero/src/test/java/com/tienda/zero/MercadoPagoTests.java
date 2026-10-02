package com.tienda.zero;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.net.MPSearchRequest;
import com.mercadopago.net.MPResultsResourcesPage;
import com.mercadopago.resources.payment.Payment;
import com.tienda.zero.controller.MercadoPagoController;
import com.tienda.zero.enums.EstadoOrdenCompra;
import com.tienda.zero.enums.TipoPago;
import com.tienda.zero.enums.TipoUsuario;
import com.tienda.zero.model.OrdenCompra;
import com.tienda.zero.model.Usuario;
import com.tienda.zero.repository.OrdenCompraRepository;
import com.tienda.zero.service.FlujoCompraService;
import com.tienda.zero.service.FacturaClienteService;
import com.tienda.zero.service.MercadoPagoService;
import com.tienda.zero.service.impl.MercadoPagoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MercadoPagoTests {
    private final OrdenCompraRepository ordenes = mock(OrdenCompraRepository.class);
    private final PaymentClient client = mock(PaymentClient.class);
    private final MercadoPagoServiceImpl servicio = new MercadoPagoServiceImpl(
            ordenes, mock(FlujoCompraService.class), client, mock(FacturaClienteService.class));

    private OrdenCompra orden(TipoUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setRol(rol);
        return OrdenCompra.builder().identificadorCompra("orden-123").propietario(usuario)
                .formaPago(TipoPago.BILLETERA_VIRTUAL).mpPreferenceId("preferencia-123")
                .estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_PAGO)
                .total(new BigDecimal("20.02")).build();
    }

    private Payment pago(String estado, String monto) {
        Payment pago = mock(Payment.class);
        when(pago.getId()).thenReturn(123L);
        when(pago.getExternalReference()).thenReturn("orden-123");
        when(pago.getStatus()).thenReturn(estado);
        when(pago.getTransactionAmount()).thenReturn(new BigDecimal(monto));
        when(pago.getCurrencyId()).thenReturn("ARS");
        return pago;
    }

    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"CLIENTE", "ADMINISTRATIVO", "JEFE"})
    void pagoAprobadoActualizaSegunElPropietario(TipoUsuario rol) throws Exception {
        OrdenCompra orden = orden(rol);
        Payment pago = pago("approved", "20.02");
        when(client.get(123L)).thenReturn(pago);
        when(ordenes.findByIdentificadorCompra("orden-123")).thenReturn(Optional.of(orden));

        servicio.procesarPago(123L);

        assertEquals(rol == TipoUsuario.CLIENTE ? EstadoOrdenCompra.PENDIENTE_ENTREGA
                : EstadoOrdenCompra.ENTREGADO, orden.getEstadoOrdenCompra());
        verify(ordenes).save(orden);
    }

    @Test
    void recuperaPagoAprobadoSinWebhookNiRetorno() throws Exception {
        OrdenCompra orden = orden(TipoUsuario.CLIENTE);
        when(ordenes.findByPropietarioNombreUsuarioAndFormaPagoAndEstadoOrdenCompraAndEliminadoFalse(
                "cliente", TipoPago.BILLETERA_VIRTUAL, EstadoOrdenCompra.PENDIENTE_PAGO))
                .thenReturn(List.of(orden));
        MPResultsResourcesPage<Payment> resultados = new MPResultsResourcesPage<>();
        resultados.setResults(List.of(pago("approved", "20.02")));
        when(client.search(any(MPSearchRequest.class))).thenReturn(resultados);

        servicio.sincronizarPagosPendientesUsuario("cliente");

        assertEquals(EstadoOrdenCompra.PENDIENTE_ENTREGA, orden.getEstadoOrdenCompra());
        verify(client).search(argThat(request -> "orden-123".equals(
                request.getFilters().get("external_reference"))
                && "approved".equals(request.getFilters().get("status"))));
    }

    @Test
    void noConfirmaPagoPendienteMontoIncorrectoNiOtraMoneda() throws Exception {
        OrdenCompra orden = orden(TipoUsuario.CLIENTE);
        when(ordenes.findByIdentificadorCompra("orden-123")).thenReturn(Optional.of(orden));
        Payment monedaIncorrecta = pago("approved", "20.02");
        when(monedaIncorrecta.getCurrencyId()).thenReturn("USD");
        Payment pendiente = pago("pending", "20.02");
        Payment montoIncorrecto = pago("approved", "20.01");
        when(client.get(123L)).thenReturn(pendiente, montoIncorrecto, monedaIncorrecta);
        for (int i = 0; i < 3; i++) {
            servicio.procesarPago(123L);
            assertEquals(EstadoOrdenCompra.PENDIENTE_PAGO, orden.getEstadoOrdenCompra());
        }
    }

    @Test
    void notificacionesRepetidasNoRetrocedenUnaOrdenEntregada() throws Exception {
        OrdenCompra orden = orden(TipoUsuario.CLIENTE);
        orden.setEstadoOrdenCompra(EstadoOrdenCompra.ENTREGADO);
        when(ordenes.findByIdentificadorCompra("orden-123")).thenReturn(Optional.of(orden));
        Payment pago = pago("approved", "20.02");
        when(client.get(123L)).thenReturn(pago);
        servicio.procesarPago(123L);
        assertEquals(EstadoOrdenCompra.ENTREGADO, orden.getEstadoOrdenCompra());
    }

    @Test
    void retornoConsultaElPagoAunqueLaUrlDigaApproved() throws Exception {
        MercadoPagoService servicio = mock(MercadoPagoService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new MercadoPagoController(servicio)).build();
        mvc.perform(get("/checkout/mercadopago/success")
                        .param("payment_id", "123").param("status", "approved"))
                .andExpect(redirectedUrl("/orders"));
        verify(servicio).procesarPago(123L);
    }

    @Test
    void webhookAceptaTipoEnElCuerpoConIdEnQuery() throws Exception {
        MercadoPagoService servicio = mock(MercadoPagoService.class);
        when(servicio.validarWebhook("firma", "request", "123")).thenReturn(true);
        var mvc = MockMvcBuilders.standaloneSetup(new MercadoPagoController(servicio)).build();
        mvc.perform(post("/mercadopago/webhook").param("data.id", "123")
                        .header("x-signature", "firma").header("x-request-id", "request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"payment\",\"data\":{\"id\":\"123\"},\"action\":\"payment.updated\"}"))
                .andExpect(status().isOk());
        verify(servicio).procesarPago(123L);
    }

    @Test
    void webhookRechazaFirmaInvalida() throws Exception {
        MercadoPagoService servicio = mock(MercadoPagoService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new MercadoPagoController(servicio)).build();
        mvc.perform(post("/mercadopago/webhook").param("data.id", "123").param("type", "payment"))
                .andExpect(status().isUnauthorized());
        verify(servicio, never()).procesarPago(any());
    }

    @Test
    void firmaAceptaEspaciosEntrePartesDelHeader() throws Exception {
        ReflectionTestUtils.setField(servicio, "webhookSecret", "secreto-test");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("secreto-test".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String firma = HexFormat.of().formatHex(mac.doFinal(
                "id:123;request-id:request;ts:1234;".getBytes(StandardCharsets.UTF_8)));
        assertTrue(servicio.validarWebhook("ts=1234, v1=" + firma, "request", "123"));
        assertFalse(servicio.validarWebhook("ts=1234, v1=" + firma, "otro-request", "123"));
    }
}
