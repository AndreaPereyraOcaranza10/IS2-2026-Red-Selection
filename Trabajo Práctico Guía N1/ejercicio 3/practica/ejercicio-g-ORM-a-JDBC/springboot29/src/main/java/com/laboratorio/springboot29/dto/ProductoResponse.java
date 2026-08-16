package com.laboratorio.springboot29.dto;

import com.laboratorio.springboot29.model.Producto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class ProductoResponse {
    private  Integer codigo;
    private String nombre;
    private double precio;

    public ProductoResponse(Producto producto) {
        this.precio = producto.getPrecio();
        this.nombre = producto.getNombre();
        this.codigo = producto.getCodigo();
    }
}