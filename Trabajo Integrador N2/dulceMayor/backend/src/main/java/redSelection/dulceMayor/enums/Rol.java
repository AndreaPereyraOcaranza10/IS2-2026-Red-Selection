package redSelection.dulceMayor.enums;

/**
 * Roles del sistema. Un único rol por usuario.
 * El JEFE_ADMINISTRATIVO hereda ADMINISTRATIVO y PREVENTISTA: la jerarquía se declara una sola vez
 * (bean RoleHierarchy en SecurityConfig) y vale para todas las reglas por URL.
 */
public enum Rol {
    CLIENTE,
    PREVENTISTA,
    TRANSPORTISTA,
    ADMINISTRATIVO,
    JEFE_ADMINISTRATIVO;

    /** Para los chequeos hechos en código (ownership), que no pasan por RoleHierarchy: ¿tiene acceso administrativo? */
    public boolean esAdministracion() {
        return this == ADMINISTRATIVO || this == JEFE_ADMINISTRATIVO;
    }
}
