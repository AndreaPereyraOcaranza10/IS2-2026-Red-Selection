package redSelection.dulceMayor.enums;

import java.util.Locale;
import java.util.Optional;

/** Formatos de descarga. La pantalla no es un formato: es el JSON del mismo reporte. */
public enum FormatoReporte {
    PDF("application/pdf", "pdf"),
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");

    private final String contentType;
    private final String extension;

    FormatoReporte(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String contentType() { return contentType; }
    public String extension() { return extension; }

    /** "pdf", "excel" o "xlsx" (en cualquier combinación de mayúsculas). */
    public static Optional<FormatoReporte> desdeTexto(String texto) {
        if (texto == null) {
            return Optional.empty();
        }
        return switch (texto.trim().toLowerCase(Locale.ROOT)) {
            case "pdf" -> Optional.of(PDF);
            case "excel", "xlsx" -> Optional.of(EXCEL);
            default -> Optional.empty();
        };
    }
}