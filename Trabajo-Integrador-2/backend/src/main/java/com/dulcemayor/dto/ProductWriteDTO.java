package com.dulcemayor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductWriteDTO(
        @NotBlank String sku,
        @NotBlank String name,
        @NotBlank String brand,
        @NotBlank String category,
        String description,
        @NotBlank String pack,
        @Min(1) int unitsPerPack,
        @Min(0) int stock,
        @Min(0) int stockMinimum,
        @NotNull @Positive BigDecimal price,
        String image,
        String color,
        boolean onSale
) {}
