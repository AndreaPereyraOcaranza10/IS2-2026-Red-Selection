package redSelection.dulceMayor.patrones.adapter;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import redSelection.dulceMayor.patrones.builder.Notificacion;

/**
 * PATRÓN: Adapter (estructural) - adaptador.
 * Adapta JavaMailSender (Spring Mail) a la interfaz EnvioCorreo.
 * Si no hay SMTP configurado (spring.mail.host) o el envío falla, devuelve false: un correo caído
 * nunca debe impedir registrar una entrega.
 */
@Component
public class JavaMailEnvioCorreo implements EnvioCorreo {

    private static final Logger log = LoggerFactory.getLogger(JavaMailEnvioCorreo.class);

    private final ObjectProvider<JavaMailSender> remitente;

    public JavaMailEnvioCorreo(ObjectProvider<JavaMailSender> remitente) {
        this.remitente = remitente;
    }

    @Override
    public boolean enviar(Notificacion notificacion) {
        JavaMailSender sender = remitente.getIfAvailable();
        if (sender == null) {
            log.warn("No hay servidor de correo configurado: no se envía '{}'", notificacion.asunto());
            return false;
        }
        try {
            MimeMessage mensaje = sender.createMimeMessage();
            MimeMessageHelper ayudante = new MimeMessageHelper(mensaje, "UTF-8");
            ayudante.setTo(notificacion.destinatario());
            ayudante.setSubject(notificacion.asunto());
            ayudante.setText(notificacion.cuerpoHtml(), true);
            sender.send(mensaje);
            return true;
        } catch (Exception ex) {
            log.error("No se pudo enviar el correo a {}", notificacion.destinatario(), ex);
            return false;
        }
    }
}
