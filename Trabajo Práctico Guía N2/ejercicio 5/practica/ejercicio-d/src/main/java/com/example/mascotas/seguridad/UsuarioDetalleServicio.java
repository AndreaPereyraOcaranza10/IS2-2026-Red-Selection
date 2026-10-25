package com.example.mascotas.seguridad;

import com.example.mascotas.entidades.Usuario;
import com.example.mascotas.repositorios.UsuarioRespositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetalleServicio implements UserDetailsService {

    @Autowired
    private UsuarioRespositorio usuarioRepositorio;

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.buscarPorMail(mail);
        if (usuario == null) {
            throw new UsernameNotFoundException("No existe un usuario con ese mail");
        }
        return new UsuarioDetalle(usuario);
    }
}