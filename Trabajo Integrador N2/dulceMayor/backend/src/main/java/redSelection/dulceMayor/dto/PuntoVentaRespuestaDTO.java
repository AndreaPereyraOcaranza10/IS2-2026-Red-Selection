package redSelection.dulceMayor.dto;

public record PuntoVentaRespuestaDTO(String id, String nombreComercio, String razonSocial, String cuit,
                                     DireccionRespuestaDTO direccion, ResponsableRespuestaDTO responsable,
                                     String preventistaId, String preventistaNombre) { }