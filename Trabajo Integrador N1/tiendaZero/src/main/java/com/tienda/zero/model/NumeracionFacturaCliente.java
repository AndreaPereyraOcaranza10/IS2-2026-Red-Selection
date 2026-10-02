package com.tienda.zero.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "numeracion_factura_cliente")
@Getter
public class NumeracionFacturaCliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
