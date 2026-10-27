package com.tienda.zero.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DatosFacturaCliente {
    private String nombre;
    private String apellido;
    private String documento;
    private String domicilio;
    private String correo;
}
