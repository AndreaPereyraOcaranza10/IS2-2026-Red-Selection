package redSelection.dulceMayor.patrones.builder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PATRÓN: Builder (creacional).
 * Una notificación por correo se arma paso a paso (destinatario, plantilla con {{marcadores}} y valores)
 * y recién {@link Builder#construir()} valida y resuelve los marcadores. El resultado es inmutable.
 */
public final class Notificacion {

    private final String destinatario;
    private final String asunto;
    private final String cuerpoHtml;

    private Notificacion(String destinatario, String asunto, String cuerpoHtml) {
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.cuerpoHtml = cuerpoHtml;
    }

    public String destinatario() { return destinatario; }
    public String asunto() { return asunto; }
    public String cuerpoHtml() { return cuerpoHtml; }

    public static Builder nueva() {
        return new Builder();
    }

    public static final class Builder {
        private String destinatario;
        private String asunto = "";
        private String plantilla = "";
        private final Map<String, String> valores = new LinkedHashMap<>();

        private Builder() { }

        public Builder para(String destinatario) { this.destinatario = destinatario; return this; }
        public Builder asunto(String asunto) { this.asunto = asunto; return this; }
        public Builder plantilla(String cuerpoHtml) { this.plantilla = cuerpoHtml; return this; }
        public Builder valor(String marcador, String valor) {
            this.valores.put(marcador, valor == null ? "" : valor);
            return this;
        }

        public Notificacion construir() {
            if (destinatario == null || destinatario.isBlank()) {
                throw new IllegalStateException("La notificación necesita un destinatario.");
            }
            String asuntoFinal = resolver(asunto);
            String cuerpoFinal = resolver(plantilla);
            if (cuerpoFinal.isBlank()) {
                throw new IllegalStateException("La notificación necesita un cuerpo.");
            }
            return new Notificacion(destinatario.trim(), asuntoFinal, cuerpoFinal);
        }

        private String resolver(String texto) {
            String resultado = texto == null ? "" : texto;
            for (Map.Entry<String, String> entrada : valores.entrySet()) {
                resultado = resultado.replace("{{" + entrada.getKey() + "}}", escaparHtml(entrada.getValue()));
            }
            return resultado;
        }

        private String escaparHtml(String texto) {
            return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
        }
    }
}
