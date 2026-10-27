package com.tienda.zero.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoInventarioDTO {
    private String id;
    private String name;
    private String code;
    private String category;
    private String talle;
    private double price;
    private String priceText;
    private double precioOferta;
    private double descuento;
    private int stock;
    private boolean enOferta;
    private String imageUrl;
}
