package com.laboratorio.springboot29.model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Producto {
    private Integer codigo;

    private String nombre;

    private double precio;

}