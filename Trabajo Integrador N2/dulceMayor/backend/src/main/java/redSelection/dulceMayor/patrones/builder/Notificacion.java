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
    private final Adjunto adjunto;

    private Notificacion(String destinatario, String asunto, String cuerpoHtml, Adjunto adjunto) {
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.cuerpoHtml = cuerpoHtml;
        this.adjunto = adjunto;
    }

    public String destinatario() { return destinatario; }
    public String asunto() { return asunto; }
    public String cuerpoHtml() { return cuerpoHtml; }
    /** Archivo adjunto (ej. la factura en PDF), o null si el correo no lleva. */
    public Adjunto adjunto() { return adjunto; }

    public record Adjunto(String nombre, String tipo, byte[] contenido) { }

    public static Builder nueva() {
        return new Builder();
    }

    public static final class Builder {
        private String destinatario;
        private String asunto = "";
        private String plantilla = "";
        private final Map<String, String> valores = new LinkedHashMap<>();
        private Adjunto adjunto;

        private Builder() { }

        public Builder adjunto(String nombre, String tipo, byte[] contenido) {
            this.adjunto = new Adjunto(nombre, tipo, contenido);
            return this;
        }

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
            if (adjunto != null && (adjunto.contenido() == null || adjunto.contenido().length == 0)) {
                throw new IllegalStateException("El adjunto " + adjunto.nombre() + " está vacío.");
            }
            return new Notificacion(destinatario.trim(), asuntoFinal, cuerpoFinal, adjunto);
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
