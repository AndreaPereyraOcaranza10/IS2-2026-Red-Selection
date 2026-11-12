// DespachoCrearDTO.java
package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Pedidos FACTURADOS que administración asigna a un transportista en un mismo despacho. */
public record DespachoCrearDTO(
        @NotBlank(message = "Elegí un transportista.") String transportistaId,
        @NotEmpty(message = "Elegí al menos un pedido.")
        @Size(max = 100, message = "Un despacho admite hasta 100 pedidos.")
        List<@NotBlank String> pedidoIds
) { }