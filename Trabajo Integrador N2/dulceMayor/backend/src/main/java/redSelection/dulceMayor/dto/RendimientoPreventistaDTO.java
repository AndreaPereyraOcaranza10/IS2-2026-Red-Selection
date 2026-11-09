package redSelection.dulceMayor.dto;

import java.math.BigDecimal;

public record RendimientoPreventistaDTO(String preventistaId, String nombrePreventista,
                                        long cantidadPedidos, long pedidosFacturados,
                                        long pedidosPendientes, long pedidosAnulados,
                                        BigDecimal montoFacturado) { }
