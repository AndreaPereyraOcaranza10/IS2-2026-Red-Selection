package com.RedSelection.videojuegos.services;

import com.RedSelection.videojuegos.entities.Usuario;
import com.RedSelection.videojuegos.enums.Rol;
import com.RedSelection.videojuegos.repositories.RepositorioUsuario;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioUsuario implements UserDetailsService {

    @Autowired
    private RepositorioUsuario repositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void validar(String nombre, String email, String clave, String confirmacion) throws Exception {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new Exception("Debe indicar el nombre");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Debe indicar el email");
        }
        if (clave == null || clave.trim().isEmpty()) {
            throw new Exception("Debe indicar la clave");
        }
        if (confirmacion == null || confirmacion.trim().isEmpty()) {
            throw new Exception("Debe indicar la confirmación de clave");
        }
        if (!clave.trim().equals(confirmacion.trim())) {
            throw new Exception("La clave debe ser igual a su confirmación");
        }
    }

    @Transactional
    public Usuario crearUsuario(String nombre, String email, String clave, String confirmacion) throws Exception {
        validar(nombre, email, clave, confirmacion);

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setRol(Rol.USER);
        usuario.setPassword(passwordEncoder.encode(clave));
        usuario.setEliminado(false);

        return repositorio.save(usuario);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = repositorio.buscarUsuarioPorEmail(email);

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }

        List<GrantedAuthority> permisos = new ArrayList<>();
        permisos.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));

        return new User(usuario.getEmail(), usuario.getPassword(), permisos);
    }
}