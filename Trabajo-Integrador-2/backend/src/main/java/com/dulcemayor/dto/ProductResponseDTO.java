package com.dulcemayor.dto;

import java.math.BigDecimal;

public record ProductResponseDTO(
        String id,
        String name,
        String brand,
        String category,
        String pack,
        int unitsPerPack,
        BigDecimal price,
        BigDecimal unitPrice,
        int stock,
        int stockMinimum,
        boolean inStock,
        String image,
        String color,
        String description,
        String tag
) {}
