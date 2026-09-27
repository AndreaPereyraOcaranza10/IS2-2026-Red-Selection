package com.tienda.zero.model;

import com.tienda.zero.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table (name = "usuario")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String nombreUsuario;

    @Column(nullable = false)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario rol;

    @Builder.Default
    @Column(nullable = false)
    private boolean Eliminado = false;

    //prueba soporte para activación de cuenta
    @Column
    private String codigoActivacion;

    @Column
    private LocalDateTime fechaExpiracionCodigo;

    @Builder.Default
    @Column(nullable = false)
    private boolean cuentaActivada = false;

}
