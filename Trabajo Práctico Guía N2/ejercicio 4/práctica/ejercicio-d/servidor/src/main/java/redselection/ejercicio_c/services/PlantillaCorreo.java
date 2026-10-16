package redselection.ejercicio_c.services;

import org.springframework.web.util.HtmlUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class PlantillaCorreo {

    private PlantillaCorreo() {
    }

    public static String cumpleanios(String nombre, String urlFacultad) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <body style="margin:0;padding:0;background:#f4f6f8;font-family:Arial,Helvetica,sans-serif;">
              <table role="presentation" width="100%" cellpadding="0" cellspacing="0">
                <tr><td align="center" style="padding:24px;">
                  <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;">
                    <tr><td style="background:#1f3a5f;color:#ffffff;padding:24px;text-align:center;font-size:24px;">
                      🎂 ¡Feliz cumpleaños!
                    </td></tr>
                    <tr><td style="padding:28px;color:#1f2933;font-size:16px;line-height:1.5;">
                      <p>Hola <strong>{{nombre}}</strong>,</p>
                      <p>Desde la facultad queremos saludarte en tu día. ¡Que tengas un excelente cumpleaños y un gran año por delante!</p>
                      <p style="text-align:center;margin-top:28px;">
                        <a href="{{url}}" style="background:#2b6cb0;color:#ffffff;text-decoration:none;padding:12px 24px;border-radius:6px;display:inline-block;">
                          Visitar la página de la facultad
                        </a>
                      </p>
                    </td></tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """
                .replace("{{nombre}}", HtmlUtils.htmlEscape(nombre))
                .replace("{{url}}", urlFacultad);
    }

    public static String vencimiento(String nombre, String titulo, LocalDate fecha) {
        return """
            <html lang="es">
            <body style="font-family:Arial,Helvetica,sans-serif;color:#1f2933;">
              <p>Hola <strong>{{nombre}}</strong>,</p>
              <p>Te recordamos que mañana, <strong>{{fecha}}</strong>, vence el plazo para devolver el libro
                 <em>{{titulo}}</em>.</p>
              <p>¡Gracias!</p>
            </body>
            </html>
            """
                .replace("{{nombre}}", HtmlUtils.htmlEscape(nombre))
                .replace("{{titulo}}", HtmlUtils.htmlEscape(titulo))
                .replace("{{fecha}}", fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }
}