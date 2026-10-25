package com.example.mascotas.seguridad;

import com.example.mascotas.repositorios.UsuarioRespositorio;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("oauth2")
public class OAuth2UsuarioServicio {
    private final UsuarioRespositorio usuarios;

    public OAuth2UsuarioServicio(UsuarioRespositorio usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public UsuarioDetalle buscar(String subject) {
        if (subject == null || subject.isBlank()) {
            throw denegado();
        }
        var usuario = usuarios.findByOauth2Subject(subject).orElseThrow(OAuth2UsuarioServicio::denegado);
        if (usuario.getBaja() != null) {
            throw denegado();
        }
        return new UsuarioDetalle(usuario);
    }

    private static OAuth2AuthenticationException denegado() {
        return new OAuth2AuthenticationException(new OAuth2Error("access_denied"),
                "La identidad no esta vinculada a un usuario local activo");
    }
}
