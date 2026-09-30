package com.tienda.zero.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoFormDTO {
    private String id;
    private String name;
    private String sku;
    private Double price;
    private String talle;
    private String idSubCategoria;
    private String description;
    private MultipartFile image;
}
