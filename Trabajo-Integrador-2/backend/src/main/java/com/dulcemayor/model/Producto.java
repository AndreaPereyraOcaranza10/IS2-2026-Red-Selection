package com.dulcemayor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;

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

    @Column(nullable = false)
    @Builder.Default
    private String marca = "";

    @Column(nullable = false)
    @Builder.Default
    private String presentacion = "Bulto cerrado";

    @Column(nullable = false)
    @Builder.Default
    private int unidadesPorBulto = 1;

    @Column(nullable = false)
    @Builder.Default
    private int stock = 0;

    @Column(nullable = false)
    @Builder.Default
    private int stockMinimo = 5;

    @Column(length = 1200)
    private String imagenUrl;

    @Column(length = 16)
    @Builder.Default
    private String colorImagen = "#f3e8dd";

    private String talle;

    @Column(nullable = false)
    @Builder.Default
    private boolean enOferta = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;

    @ManyToOne
    @JoinColumn(name = "subcategoria_id", nullable = false)
    private SubCategoria subCategoria;

    //Relación para la imágen
    @ManyToOne
    @JoinColumn(name = "imagen_id")
    private Imagen imagen;

}
