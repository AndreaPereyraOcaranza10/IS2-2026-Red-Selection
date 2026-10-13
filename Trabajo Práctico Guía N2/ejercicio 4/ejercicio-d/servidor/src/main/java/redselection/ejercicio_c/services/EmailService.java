package redselection.ejercicio_c.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String remitente;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    public void enviarHtml(String destinatario, String asunto, String html) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(html, true); // true = es HTML
            mailSender.send(mensaje);
        } catch (MessagingException e) {
            throw new IllegalStateException("No se pudo armar el correo para " + destinatario, e);
        }
    }
}