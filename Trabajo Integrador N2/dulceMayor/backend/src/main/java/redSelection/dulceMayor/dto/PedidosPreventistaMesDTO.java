package redSelection.dulceMayor.dto;

import java.math.BigDecimal;

public record PedidosPreventistaMesDTO(String preventistaId, String nombrePreventista, String periodo,
                                       long cantidadPedidos, BigDecimal montoPedidos) { }
