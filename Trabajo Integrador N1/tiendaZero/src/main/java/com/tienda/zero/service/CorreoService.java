package com.tienda.zero.service;

import org.springframework.core.io.Resource;

public interface CorreoService {
    void enviarCorreoHtml(String destinatario, String asunto, String cuerpoHtml);

    void enviarCorreoHtmlConImagenInline(String destinatario, String asunto, String cuerpoHtml,
                                         String contentId, Resource imagen, String contentType);
}
