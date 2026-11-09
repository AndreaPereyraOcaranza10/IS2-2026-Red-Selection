package redSelection.dulceMayor.dto;

import java.math.BigDecimal;

public record PedidosPreventistaDiaDTO(String preventistaId, String nombrePreventista, String fecha,
                                       long cantidadPedidos, BigDecimal montoPedidos) { }
