package com.tienda.zero.model;

import com.tienda.zero.enums.TipoImagen;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "imagen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String nombre;

    private String mime;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] contenido;

    @Enumerated(EnumType.STRING)
    private TipoImagen tipoImagen;

    @Builder.Default
    private boolean eliminado = false;
}