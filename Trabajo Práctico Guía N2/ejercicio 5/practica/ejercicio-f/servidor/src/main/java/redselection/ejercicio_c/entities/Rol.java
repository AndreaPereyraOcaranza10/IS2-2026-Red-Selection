package redselection.ejercicio_c.entities;

/**
 * Roles del sistema. Spring Security los usa con el prefijo "ROLE_" (ver Usuario.getAuthorities()).
 */
public enum Rol {
    USER,   // puede consultar, crear y modificar
    ADMIN   // además puede eliminar
}
