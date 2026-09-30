package com.tienda.zero.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "producto", indexes = {
        @Index(name = "idx_producto_nombre", columnList = "nombre")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    private String talle;

    @Column(nullable = false)
    @Builder.Default
    private boolean enOferta = false;

    @Column(nullable = false, columnDefinition = "double default 0")
    @Builder.Default
    private double porcentajeDescuento = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;

    @JsonIgnore
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Stock> stocks = new ArrayList<>();

    @Transient
    private int stockActual;

    @ManyToOne
    @JoinColumn(name = "subcategoria_id", nullable = false)
    private SubCategoria subCategoria;

    //Relación para la imágen
    @JsonIgnore @ManyToOne
    @JoinColumn(name = "imagen_id")
    private Imagen imagen;

}
