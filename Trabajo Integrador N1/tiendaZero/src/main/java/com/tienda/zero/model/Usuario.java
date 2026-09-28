package com.tienda.zero.model;

import com.tienda.zero.enums.Rol;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad Usuario representada en el diagrama de clases UML.
 * Maneja el acceso al sistema mediante correo electrónico (usuario) y contraseña encriptada.
 * Se vincula de forma 1 a 1 con la Persona (Cliente o Empleado).
 */
@Entity
@Table(name = "usuario")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * El correo personal del usuario, utilizado como nombre de usuario (identificador de login).
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Contraseña almacenada de forma encriptada mediante algoritmo BCrypt.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Rol asignado: ADMIN o CLIENTE.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    /**
     * Indica si la cuenta ya ha sido activada mediante el código recibido por correo.
     * Los clientes inician en false hasta ingresar el código.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean activo = false;

    /**
     * Código de 6 dígitos generado al registrarse, enviado por correo para activar la cuenta.
     */
    @Column
    private String codigoActivacion;

    /**
     * Fecha y hora límite para ingresar el código de activación (24 horas).
     */
    @Column
    private LocalDateTime fechaExpiracionCodigo;

    /**
     * Borrado lógico según estándar del modelo.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;

    /**
     * Relación con los datos personales (Persona / Cliente / Empleado) indicada en el diagrama UML.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "persona_id")
    private Persona persona;
}
