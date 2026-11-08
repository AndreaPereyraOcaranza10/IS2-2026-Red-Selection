package redSelection.dulceMayor.security;

/**
 * ¿Sigue valiendo este token? Un JWT firmado y sin vencer no alcanza: el usuario pudo desactivarse,
 * borrarse o cerrar sesión después de emitido. Es una interfaz para que SecurityConfig y los tests no
 * dependan de la base de datos.
 */
public interface ControlDeSesion {
    boolean estaVigente(UsuarioAutenticado usuario);
}
