package redSelection.dulceMayor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Alta de un pedido. Los nombres en inglés son el contrato que ya usan los frontends.
 * El administrativo indica clientName; el preventista, pointOfSaleId (id del comercio) y opcionalmente promoCode;
 * el cliente no indica comercio (se toma el suyo).
 */
public record PedidoCrearDTO(String clientName, String paymentMethod, String pointOfSaleId, String promoCode,
                             @NotEmpty(message = "El pedido debe incluir productos") List<@Valid Linea> items) {

    public record Linea(@NotBlank String productId,
                        @Min(value = 1, message = "Las cantidades deben ser mayores a cero") int quantity) { }
}
