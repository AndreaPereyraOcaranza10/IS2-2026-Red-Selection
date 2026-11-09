package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.*;
import redSelection.dulceMayor.enums.ProveedorAuth;
import redSelection.dulceMayor.enums.Rol;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.time.LocalDateTime;

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
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 32)
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

    /* Protección contra fuerza bruta: fallos consecutivos y, si se superó el máximo, hasta cuándo está bloqueado. */
    @Column(nullable = false)
    @Builder.Default
    private int intentosFallidos = 0;

    private LocalDateTime bloqueadoHasta;

    /* Se incrementa para invalidar todos los tokens ya emitidos (logout, cambio de clave). El JWT lleva esta versión. */
    @Column(nullable = false)
    @Builder.Default
    private int versionToken = 0;

    // --- Comportamiento (patrón Experto: el usuario sabe cuándo está bloqueado) ---

    public boolean estaBloqueado(LocalDateTime ahora) {
        return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
    }

    /** Suma un fallo; al llegar al máximo bloquea por la duración indicada y reinicia el contador. */
    public void registrarFallo(int maxIntentos, Duration bloqueo, LocalDateTime ahora) {
        if (bloqueadoHasta != null && !ahora.isBefore(bloqueadoHasta)) {   // el bloqueo anterior ya venció
            bloqueadoHasta = null;
            intentosFallidos = 0;
        }
        intentosFallidos++;
        if (intentosFallidos >= maxIntentos) {
            bloqueadoHasta = ahora.plus(bloqueo);
            intentosFallidos = 0;
        }
    }

    public void registrarAcierto() {
        intentosFallidos = 0;
        bloqueadoHasta = null;
    }

    /** Deja sin efecto todos los tokens emitidos hasta ahora. */
    public void revocarTokens() {
        versionToken++;
    }
}
