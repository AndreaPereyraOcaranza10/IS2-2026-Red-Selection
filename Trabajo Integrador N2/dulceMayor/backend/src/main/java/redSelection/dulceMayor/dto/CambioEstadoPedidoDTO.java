package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.NotBlank;

/** Cambio de estado pedido por administración. note es opcional y queda como observación en el historial. */
public record CambioEstadoPedidoDTO(@NotBlank String status, String note) { }
