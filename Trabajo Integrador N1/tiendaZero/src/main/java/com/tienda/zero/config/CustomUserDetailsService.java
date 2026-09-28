package com.tienda.zero.config;

import com.tienda.zero.model.Usuario;
import com.tienda.zero.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró ningún usuario con el correo: " + email));

        if (usuario.isEliminado()) {
            throw new DisabledException("La cuenta ha sido dada de baja.");
        }

        // Si la cuenta no está activa, lanzamos DisabledException para guiar al usuario a la página de activación
        if (!usuario.isActivo()) {
            throw new DisabledException("La cuenta aún no ha sido activada. Por favor ingresa el código enviado a tu correo.");
        }

        // Se asigna el rol con el prefijo estándar de Spring Security ROLE_
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());

        return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.isActivo(),
                true,
                true,
                !usuario.isEliminado(),
                Collections.singletonList(authority)
        );
    }
}
