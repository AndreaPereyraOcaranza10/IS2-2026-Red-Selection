package com.tienda.zero.service.impl;

import com.tienda.zero.service.CorreoService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class CorreoServiceImpl implements CorreoService {

    private final JavaMailSender mailSender;
    private final String remitente;

    public CorreoServiceImpl(JavaMailSender mailSender, @Value("${app.mail.from}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public void enviarCorreoHtml(String destinatario, String asunto, String cuerpoHtml) {
        enviar(destinatario, asunto, cuerpoHtml, null, null, null);
    }

    @Override
    public void enviarCorreoHtmlConImagenInline(String destinatario, String asunto, String cuerpoHtml,
                                                String contentId, Resource imagen, String contentType) {
        enviar(destinatario, asunto, cuerpoHtml, contentId, imagen, contentType);
    }

    @Override
    public void enviarCorreoHtmlConAdjunto(String destinatario, String asunto, String cuerpoHtml,
                                          String nombreArchivo, byte[] contenido, String contentType) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true);
            helper.addAttachment(nombreArchivo, new ByteArrayResource(contenido), contentType);
            mailSender.send(mensaje);
        } catch (MessagingException e) {
            throw new IllegalStateException("No se pudo enviar el correo de la factura", e);
        }
    }

    private void enviar(String destinatario, String asunto, String cuerpoHtml,
                        String contentId, Resource imagen, String contentType) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, imagen != null, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true);
            if (imagen != null) {
                helper.addInline(contentId, imagen, contentType);
            }

            mailSender.send(mensaje);
        } catch (MessagingException e) {
            throw new IllegalStateException("No se pudo enviar el correo a " + destinatario, e);
        }
    }
}
