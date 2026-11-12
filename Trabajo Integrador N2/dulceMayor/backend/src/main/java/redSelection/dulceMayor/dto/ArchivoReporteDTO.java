package redSelection.dulceMayor.dto;

/** Reporte exportado: lo que el controller devuelve como descarga. */
public record ArchivoReporteDTO(String nombreArchivo, String contentType, byte[] contenido) { }