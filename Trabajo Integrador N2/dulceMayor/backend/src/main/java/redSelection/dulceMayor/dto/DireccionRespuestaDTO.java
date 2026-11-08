package redSelection.dulceMayor.dto;

public record DireccionRespuestaDTO(String calle, String numeracion, String barrio, String referencia,
                                    String provinciaId, String provinciaNombre,
                                    String departamentoId, String departamentoNombre,
                                    String localidadId, String localidadNombre,
                                    Double latitud, Double longitud, String textoCompleto) { }