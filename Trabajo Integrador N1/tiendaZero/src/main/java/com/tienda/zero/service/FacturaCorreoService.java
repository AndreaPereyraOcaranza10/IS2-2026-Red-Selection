package com.tienda.zero.service;

import com.tienda.zero.enums.EstadoFactura;
import com.tienda.zero.repository.FacturaClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FacturaCorreoService {
    private final FacturaClienteRepository facturas;
    private final CorreoService correos;
    private final SpringTemplateEngine plantillas;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enviar(String facturaId) {
        var factura = facturas.buscarParaEnviar(facturaId).orElse(null);
        if (factura == null || factura.isEliminado() || !factura.isCorreoPendiente()
                || factura.isCorreoEnviado() || factura.getEstado() == EstadoFactura.ANULADA) return;
        Context contexto = new Context(Locale.forLanguageTag("es-AR"));
        contexto.setVariable("factura", factura);
        String html = plantillas.process("correos/factura-cliente", contexto);
        correos.enviarCorreoHtmlConAdjunto(factura.getCorreoCliente(),
                "Tu factura Nro. " + factura.getNumeroFactura() + " - Zero", html,
                "factura-" + factura.getNumeroFactura() + ".html", html.getBytes(StandardCharsets.UTF_8),
                "text/html; charset=UTF-8");
        factura.setCorreoEnviado(true);
        factura.setCorreoPendiente(false);
    }

    @Transactional(readOnly = true)
    public List<String> pendientes() {
        return facturas.findTop20ByCorreoPendienteTrueAndCorreoEnviadoFalseAndEliminadoFalseOrderByFechaFacturaAscNumeroFacturaAsc()
                .stream().map(f -> f.getId()).toList();
    }
}
