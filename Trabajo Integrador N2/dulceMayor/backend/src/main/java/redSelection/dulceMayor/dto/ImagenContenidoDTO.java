package redSelection.dulceMayor.dto;

public record ImagenContenidoDTO(String mime, byte[] contenido) {}   // lo que necesita el controller para servir la imagen