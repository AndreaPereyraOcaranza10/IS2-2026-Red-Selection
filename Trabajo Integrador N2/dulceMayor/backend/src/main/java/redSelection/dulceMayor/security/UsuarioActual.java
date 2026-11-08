package redSelection.dulceMayor.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Quién hace el pedido. Los services lo usan para filtrar y validar propiedad sin que el controller les pase el usuario. */
@Component
public class UsuarioActual {

    public UsuarioAutenticado obtener() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new AccessDeniedException("No hay un usuario autenticado");
        }
        return usuario;
    }
}