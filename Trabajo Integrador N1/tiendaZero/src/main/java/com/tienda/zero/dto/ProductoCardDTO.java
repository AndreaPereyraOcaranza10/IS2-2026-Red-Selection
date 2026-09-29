package com.tienda.zero.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoCardDTO {
    private String id;
    private String name;
    private String category;
    private String categorySlug;
    private double price;
    private Double oldPrice;
    private String imageUrl;
    private String description;
    private String sku;
    private String brand;
    private String talle;

    @Builder.Default
    private boolean inStock = true;

    @Builder.Default
    private int reviewCount = 5;

    @Builder.Default
    private int rating = 5;

    public String getMainImageUrl() {
        return (imageUrl != null && !imageUrl.isBlank()) ? imageUrl : "/assets/images/products/1.jpg";
    }

    public List<String> getGalleryImages() {
        String main = getMainImageUrl();
        return List.of(main);
    }
}

