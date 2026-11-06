package com.dulcemayor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import java.util.List;

public record OrderCreateDTO(
        @NotBlank String clientName,
        String paymentMethod,
        @NotEmpty List<@Valid Line> items
) {
    public record Line(@NotBlank String productId, @Min(1) int quantity) {}
}
