package com.tienda.zero.service;

import org.springframework.core.io.Resource;

public interface CorreoService {
    void enviarCorreoHtml(String destinatario, String asunto, String cuerpoHtml);

    void enviarCorreoHtmlConAdjunto(String destinatario, String asunto, String cuerpoHtml,
                                   String nombreArchivo, byte[] contenido, String contentType);

    void enviarCorreoHtmlConImagenInline(String destinatario, String asunto, String cuerpoHtml,
                                         String contentId, Resource imagen, String contentType);
}
