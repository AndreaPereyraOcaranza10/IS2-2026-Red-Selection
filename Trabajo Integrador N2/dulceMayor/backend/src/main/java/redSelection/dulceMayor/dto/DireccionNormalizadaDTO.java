package redSelection.dulceMayor.dto;

/* Resultado de normalizar una dirección. Las coordenadas pueden ser null si la API no las estima. */
public record DireccionNormalizadaDTO(String nomenclatura, Double latitud, Double longitud) { }