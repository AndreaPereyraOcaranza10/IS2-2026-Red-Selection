package com.tienda.zero;

import com.tienda.zero.enums.EstadoFactura;
import com.tienda.zero.enums.TipoPago;
import com.tienda.zero.model.*;
import com.tienda.zero.repository.FacturaClienteRepository;
import com.tienda.zero.service.CorreoService;
import com.tienda.zero.service.FacturaCorreoService;
import com.tienda.zero.service.impl.CorreoServiceImpl;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FacturaCorreoTests {
    private final FacturaClienteRepository repository = mock(FacturaClienteRepository.class);
    private final CorreoService correos = mock(CorreoService.class);
    private final FacturaCorreoService servicio = new FacturaCorreoService(repository, correos, plantillas());

    private SpringTemplateEngine plantillas() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    private FacturaCliente factura() {
        FacturaCliente factura = FacturaCliente.builder().id("factura-test").numeroFactura(42)
                .fechaFactura(Date.valueOf("2026-10-01")).nombreCliente("Ana <Perez>")
                .apellidoCliente("Gomez").domicilioCliente("Calle Uno 100").correoCliente("comprador@zero.test")
                .formaDePago(FormaDePago.builder().tipoPago(TipoPago.BILLETERA_VIRTUAL).build())
                .estado(EstadoFactura.PAGADA).correoPendiente(true).totalPagado(20.02)
                .ordenCompra(OrdenCompra.builder().identificadorCompra("orden-test").build()).build();
        factura.getDetalles().add(DetalleFactura.builder().cantidad(2).subtotal(20.02)
                .nombreProducto("Remera <script>alert(1)</script>").codigoProducto("REM-1").build());
        when(repository.buscarParaEnviar("factura-test")).thenReturn(Optional.of(factura));
        return factura;
    }

    @Test
    void enviaDetalleYAdjuntoSinRepetirLaFactura() {
        FacturaCliente factura = factura();
        servicio.enviar("factura-test");
        servicio.enviar("factura-test");
        verify(correos, times(1)).enviarCorreoHtmlConAdjunto(eq("comprador@zero.test"), contains("42"),
                argThat(html -> html.contains("Ana &lt;Perez&gt;") && html.contains("orden-test")
                        && html.contains("REM-1") && html.contains("20,02") && !html.contains("<script>")),
                eq("factura-42.html"), argThat(bytes -> new String(bytes, StandardCharsets.UTF_8).contains("00000042")),
                eq("text/html; charset=UTF-8"));
        assertTrue(factura.isCorreoEnviado());
        assertFalse(factura.isCorreoPendiente());
    }

    @Test
    void errorDeCorreoMantieneElEnvioPendienteParaReintentar() {
        FacturaCliente factura = factura();
        doThrow(new IllegalStateException("SMTP no disponible")).doNothing().when(correos)
                .enviarCorreoHtmlConAdjunto(anyString(), anyString(), anyString(), anyString(), any(byte[].class), anyString());
        assertThrows(IllegalStateException.class, () -> servicio.enviar("factura-test"));
        assertTrue(factura.isCorreoPendiente());
        assertFalse(factura.isCorreoEnviado());
        servicio.enviar("factura-test");
        assertTrue(factura.isCorreoEnviado());
    }

    @Test
    void noEnviaUnaFacturaAnulada() {
        FacturaCliente factura = factura();
        factura.setEstado(EstadoFactura.ANULADA);
        servicio.enviar("factura-test");
        verifyNoInteractions(correos);
    }

    @Test
    void smtpIncluyeLaFacturaComoArchivoAdjunto() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage mensaje = new MimeMessage(Session.getInstance(new Properties()));
        when(sender.createMimeMessage()).thenReturn(mensaje);
        byte[] contenido = "<html>Factura 42</html>".getBytes(StandardCharsets.UTF_8);
        new CorreoServiceImpl(sender, "zero@zero.test").enviarCorreoHtmlConAdjunto(
                "comprador@zero.test", "Factura 42", "<p>Tu factura</p>", "factura-42.html", contenido, "text/html; charset=UTF-8");
        mensaje.saveChanges();
        assertEquals("comprador@zero.test", mensaje.getAllRecipients()[0].toString());
        MimeMultipart multipart = (MimeMultipart) mensaje.getContent();
        boolean encontrado = false;
        for (int i = 0; i < multipart.getCount(); i++) {
            var parte = multipart.getBodyPart(i);
            if ("factura-42.html".equals(parte.getFileName())) {
                assertArrayEquals(contenido, parte.getInputStream().readAllBytes());
                encontrado = true;
            }
        }
        assertTrue(encontrado);
        verify(sender).send(mensaje);
    }
}
