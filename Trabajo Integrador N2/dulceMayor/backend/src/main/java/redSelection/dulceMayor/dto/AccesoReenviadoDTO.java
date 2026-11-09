package redSelection.dulceMayor.dto;

/** Respuesta de "reenviar acceso": a qué correo se mandó la nueva clave temporal (la clave nunca viaja en la API). */
public record AccesoReenviadoDTO(String correo, String mensaje) { }