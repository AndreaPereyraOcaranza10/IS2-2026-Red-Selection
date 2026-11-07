package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import redSelection.dulceMayor.enums.ProveedorAuth;
import redSelection.dulceMayor.enums.Rol;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "usuario",
        uniqueConstraints = @UniqueConstraint(columnNames = {"proveedor_auth", "id_externo"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Usuario extends Base {

    /* Empleados: nombre de usuario. Clientes: su correo electrónico. */
    @Column(nullable = false, unique = true)
    private String nombreUsuario;

    /* Hash BCrypt, nunca texto plano. Es null si el cliente ingresa con una red social. */
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProveedorAuth proveedorAuth = ProveedorAuth.LOCAL;

    /* Identificador de la cuenta en la red social (login con Google). */
    private String idExterno;

    /* true mientras el empleado tenga la clave por defecto: debe cambiarla antes de usar el sistema. */
    @Column(nullable = false)
    @Builder.Default
    private boolean primerIngreso = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;
}