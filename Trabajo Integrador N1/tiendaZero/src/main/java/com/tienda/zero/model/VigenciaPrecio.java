package com.tienda.zero.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;

@Entity
@Table(name = "historial_precios")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class VigenciaPrecio {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private String id;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @Column(nullable = false)
    private Date fechaDesde;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @Column
    private Date fechaHasta;

    @Column(nullable = false)
    private double precio;

    @Column(nullable = false)
    private boolean eliminado;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

}
