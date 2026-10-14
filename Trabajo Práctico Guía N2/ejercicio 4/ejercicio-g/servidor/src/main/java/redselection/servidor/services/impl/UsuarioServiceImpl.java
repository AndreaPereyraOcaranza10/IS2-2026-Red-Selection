package redselection.servidor.services.impl;

import org.springframework.stereotype.Service;
import redselection.servidor.model.Usuario;
import redselection.servidor.repositories.UsuarioRepository;
import redselection.servidor.services.UsuarioService;

@Service
public class UsuarioServiceImpl extends BaseServiceImpl<Usuario> implements UsuarioService {

    public UsuarioServiceImpl(UsuarioRepository repository) {
        super(repository);
    }

    @Override
    protected void validar(Usuario u) {
        if (u.getNombreUsuario() == null || u.getNombreUsuario().isBlank())
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        if (u.getClave() == null || u.getClave().isBlank())
            throw new IllegalArgumentException("La clave es obligatoria");
    }
}