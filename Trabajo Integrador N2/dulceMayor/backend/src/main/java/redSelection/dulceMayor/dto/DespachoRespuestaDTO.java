// DespachoRespuestaDTO.java
package redSelection.dulceMayor.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DespachoRespuestaDTO(String transportistaId, String transportista,
                                   List<String> pedidoIds, LocalDateTime fechaDespacho) { }