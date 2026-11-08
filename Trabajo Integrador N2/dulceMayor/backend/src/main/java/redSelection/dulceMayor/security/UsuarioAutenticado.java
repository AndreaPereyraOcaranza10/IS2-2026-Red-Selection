package redSelection.dulceMayor.security;

import java.security.Principal;

import redSelection.dulceMayor.enums.Rol;

/**
 * Usuario que viene dentro del token JWT. Es el "principal" de Spring Security: los controllers lo reciben
 * con @AuthenticationPrincipal y las reglas de acceso comparan su empleadoId con el id de la URL.
 */
public final class UsuarioAutenticado implements Principal {

    private final String id;
    private final String nombreUsuario;
    private final Rol rol;
    private final boolean primerIngreso;
    private final String empleadoId;

    public UsuarioAutenticado(String id, String nombreUsuario, Rol rol, boolean primerIngreso, String empleadoId) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
        this.primerIngreso = primerIngreso;
        this.empleadoId = empleadoId;
    }

    public String getId() { return id; }
    public String getNombreUsuario() { return nombreUsuario; }
    @Override public String getName() { return nombreUsuario; }
    public Rol getRol() { return rol; }
    public boolean isPrimerIngreso() { return primerIngreso; }
    /** Id de la persona (empleado) dueña del usuario; es el que usan las rutas. Puede ser null (ej. un cliente sin ficha). */
    public String getEmpleadoId() { return empleadoId; }
}
