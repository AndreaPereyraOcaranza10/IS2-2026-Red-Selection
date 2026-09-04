package com.ejercicio_c.biblioteca.services;

import com.ejercicio_c.biblioteca.entities.Imagen;
import com.ejercicio_c.biblioteca.entities.Usuario;
import com.ejercicio_c.biblioteca.enums.Rol;
import com.ejercicio_c.biblioteca.exceptions.ErrorServiceException;
import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import com.ejercicio_c.biblioteca.repositories.UsuarioRepositorio;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepositorio repository;

    @Autowired
    private ImagenService imagenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void validar(String nombre, String email, String clave, String confirmacion) throws ErrorServiceException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el nombre");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el Email");
        }

        if (clave == null || clave.trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar la clave");
        }

        if (confirmacion == null || confirmacion.trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar la confirmación de clave");
        }

        if (!clave.trim().equals(confirmacion.trim())) {
            throw new ErrorServiceException("La clave debe ser igual a su confirmación");
        }
    }

    @Transactional
    public Usuario crearUsuario(String nombre, String email, String clave, String confirmacion, MultipartFile archivo) throws ErrorServiceException {
        validar(nombre, email, clave, confirmacion);

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setRol(Rol.USER);
        usuario.setPassword(passwordEncoder.encode(clave));
        usuario.setEliminado(false);

        if (archivo != null && !archivo.isEmpty()) {
            Imagen imagen = imagenService.crearImagen(archivo);
            usuario.setImagen(imagen);
        }

        return repository.save(usuario);
    }

    @Transactional
    public Usuario modificarUsuario(String idUsuario, String nombre, String email, String clave, String confirmacion, MultipartFile archivo) throws ErrorServiceException {
        validar(nombre, email, clave, confirmacion);

        Usuario usuario = buscarUsuario(idUsuario);
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(clave));

        if (archivo != null && !archivo.isEmpty()) {
            String idImagen = null;
            if (usuario.getImagen() != null) {
                idImagen = usuario.getImagen().getId();
            }
            Imagen imagen = imagenService.modificarImagen(idImagen, archivo);
            usuario.setImagen(imagen);
        }

        return repository.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(String idUsuario) throws ErrorServiceException {
        Usuario usuario = buscarUsuario(idUsuario);
        usuario.setEliminado(true);
        repository.save(usuario);
    }

    @Transactional
    public void cambiarRol(String idUsuario) throws ErrorServiceException {
        Usuario usuario = buscarUsuario(idUsuario);

        if (usuario.getRol() == Rol.ADMIN) {
            usuario.setRol(Rol.USER);
        } else {
            usuario.setRol(Rol.ADMIN);
        }

        repository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarUsuario(String idUsuario) throws ErrorServiceException {
        if (idUsuario == null || idUsuario.trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el usuario");
        }

        Usuario usuario = repository.findById(idUsuario)
                .orElseThrow(() -> new ErrorServiceException("No se encuentra el usuario indicado"));

        if (usuario.isEliminado()) {
            throw new ErrorServiceException("El usuario se encuentra dado de baja");
        }

        return usuario;
    }

    @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorEmail(String email) throws ErrorServiceException {
        if (email == null || email.trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el email");
        }

        Usuario usuario = repository.buscarUsuarioPorEmail(email);
        if (usuario == null || usuario.isEliminado()) {
            throw new ErrorServiceException("No se encuentra el usuario con el email indicado");
        }

        return usuario;
    }

    @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorNombre(String nombre) throws ErrorServiceException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el nombre");
        }

        Usuario usuario = repository.buscarUsuarioPorNombre(nombre);
        if (usuario == null || usuario.isEliminado()) {
            throw new ErrorServiceException("No se encuentra el usuario con el nombre indicado");
        }

        return usuario;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuario() {
        return repository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = repository.buscarUsuarioPorEmail(email);

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }

        if (usuario.isEliminado()) {
            throw new UsernameNotFoundException("El usuario se encuentra inhabilitado");
        }

        List<GrantedAuthority> permisos = new ArrayList<>();
        GrantedAuthority p = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());
        permisos.add(p);

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession(true);
        session.setAttribute("usuariosession", usuario);

        return new User(usuario.getEmail(), usuario.getPassword(), permisos);
    }
}