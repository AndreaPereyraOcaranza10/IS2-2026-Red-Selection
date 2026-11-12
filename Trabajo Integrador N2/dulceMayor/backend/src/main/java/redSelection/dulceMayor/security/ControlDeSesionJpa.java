package redSelection.dulceMayor.security;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.model.Usuario;
import redSelection.dulceMayor.service.UsuarioService;

/** Revocación de tokens: el usuario tiene que existir, estar activo y tener la misma versionToken que el JWT. */
@Component
public class ControlDeSesionJpa implements ControlDeSesion {

    private final UsuarioService usuarioService;

    public ControlDeSesionJpa(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaVigente(UsuarioAutenticado usuario) {
        return usuarioService.buscarActivoPorId(usuario.getId())
                .filter(Usuario::isActivo)
                .filter(u -> u.getVersionToken() == usuario.getVersion())
                .isPresent();
    }
}