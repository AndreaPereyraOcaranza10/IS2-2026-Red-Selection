package redSelection.dulceMayor.dto;

import java.util.List;

public record SucursalRespuestaDTO(String id, String razonSocial, String cuit, String calle, String numeracion,
                                   String barrio, String manzanaPiso, String casaDepartamento, String referencia,
                                   String localidadId, String localidad, String departamentoId, String departamento,
                                   String provinciaId, String provincia, String email, List<String> photos) { }