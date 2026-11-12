package redSelection.dulceMayor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import redSelection.dulceMayor.dto.NewsletterEnvioRespuestaDTO;
import redSelection.dulceMayor.patrones.facade.NotificacionFacade;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** BK-12: envío manual o automático de las ofertas vigentes a los clientes minoristas activos. */
@Service
public class NewsletterService {

    private static final Logger log = LoggerFactory.getLogger(NewsletterService.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-AR"));

    private final NewsletterConsultaService consulta;
    private final NotificacionFacade notificaciones;
    private final TaskScheduler taskScheduler;
    private final ZoneId zona;

    public NewsletterService(NewsletterConsultaService consulta, NotificacionFacade notificaciones,
                             TaskScheduler newsletterTaskScheduler,
                             @Value("${app.newsletter.zona:America/Argentina/Mendoza}") String zona) {
        this.consulta = consulta;
        this.notificaciones = notificaciones;
        this.taskScheduler = newsletterTaskScheduler;
        this.zona = ZoneId.of(zona);
    }

    public Instant programarUnaVez(long minutos) {
        Instant envioEn = Instant.now().plus(Duration.ofMinutes(minutos));
        taskScheduler.schedule(() -> {
            try {
                NewsletterEnvioRespuestaDTO resumen = enviarAhora();
                log.info("Newsletter manual programada: {} ofertas, {} destinatarios, {} enviados, {} fallidos",
                        resumen.productosIncluidos(), resumen.destinatarios(), resumen.enviados(), resumen.fallidos());
            } catch (RuntimeException ex) {
                log.error("Falló el envío único programado de la newsletter", ex);
            }
        }, envioEn);
        return envioEn;
    }

    public NewsletterEnvioRespuestaDTO enviarAhora() {
        LocalDate hoy = LocalDate.now(zona);
        NewsletterConsultaService.DatosNewsletter datos = consulta.obtener(hoy);
        int enviados = 0;
        int fallidos = 0;
        if (!datos.ofertas().isEmpty()) {
            String productosHtml = renderizarOfertas(datos);
            String fecha = FECHA.format(datos.fecha());
            for (NewsletterConsultaService.Destinatario destinatario : datos.destinatarios()) {
                try {
                    if (notificaciones.enviarNewsletter(destinatario.correo(), destinatario.nombre(), fecha, productosHtml)) {
                        enviados++;
                    } else {
                        fallidos++;
                    }
                } catch (RuntimeException ex) {
                    fallidos++;
                    log.error("No se pudo preparar el correo de newsletter para un destinatario", ex);
                }
            }
        }
        return new NewsletterEnvioRespuestaDTO(datos.fecha(), datos.ofertas().size(), datos.destinatarios().size(),
                enviados, fallidos, datos.clientesSinCorreo());
    }

    @Scheduled(cron = "${app.newsletter.cron:0 0 9 5 * *}", zone = "${app.newsletter.zona:America/Argentina/Mendoza}")
    public void enviarMensual() {
        try {
            NewsletterEnvioRespuestaDTO resumen = enviarAhora();
            log.info("Newsletter mensual: {} ofertas, {} destinatarios, {} enviados, {} fallidos, {} clientes sin correo",
                    resumen.productosIncluidos(), resumen.destinatarios(), resumen.enviados(),
                    resumen.fallidos(), resumen.clientesSinCorreo());
        } catch (RuntimeException ex) {
            log.error("Falló la ejecución mensual de la newsletter", ex);
        }
    }

    private String renderizarOfertas(NewsletterConsultaService.DatosNewsletter datos) {
        NumberFormat pesos = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));
        StringBuilder html = new StringBuilder();
        for (NewsletterConsultaService.Oferta oferta : datos.ofertas()) {
            html.append("<li><b>").append(escaparHtml(oferta.nombre())).append("</b>");
            if (oferta.marca() != null && !oferta.marca().isBlank()) {
                html.append(" · ").append(escaparHtml(oferta.marca()));
            }
            html.append(" — ").append(escaparHtml(pesos.format(oferta.precio())))
                    .append(" <small>(stock: ").append(oferta.stock()).append(" u.)</small></li>");
        }
        return html.toString();
    }

    private static String escaparHtml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
