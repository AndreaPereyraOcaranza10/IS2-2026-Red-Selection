package com.tienda.zero.security;

import com.tienda.zero.model.Usuario;
import com.tienda.zero.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/*
 * Conecta los usuarios de la tienda con Spring Security.
 * Spring llama a loadUserByUsername en cada intento de login: busca el usuario
 * por nombre y devuelve su clave encriptada, su rol y si la cuenta está habilitada
 * (un usuario eliminado queda deshabilitado y no puede entrar).
 * La comparación de la clave la hace Spring con el PasswordEncoder de SecurityConfig.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        Optional<Usuario> resultado = usuarioRepository.findByNombreUsuario(nombreUsuario);
        if (resultado.isEmpty()) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + nombreUsuario);
        }
        Usuario usuario = resultado.get();

        return User.withUsername(usuario.getNombreUsuario())
                .password(usuario.getClave())
                .roles(usuario.getRol().name())
                .disabled(usuario.isEliminado() || !usuario.isCuentaActivada())
                .build();
    }
}