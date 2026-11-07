package redSelection.dulceMayor.enums;

/**
 * Roles del sistema según el diagrama de clases y especificación:
 * - ADMIN: Administrador con acceso al panel y gestión general. --> mismo rol que el almacenero
 * - CLIENTE: Cliente de la tienda que puede comprar y gestionar su perfil.
 * Un único rol por usuario. El Jefe hereda los permisos de Administrativo y Preventista (RoleHierarchy)
 */
public enum Rol {
    CLIENTE,
    PREVENTISTA,
    TRANSPORTISTA,
    ADMINISTRATIVO
}
