package com.tienda.zero.service;

import com.tienda.zero.enums.TipoUsuario;
import com.tienda.zero.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UsuarioService {

    Usuario crearUsuario(String nombreUsuario, String clave, TipoUsuario rol);

    void validar(String nombreUsuario, String clave, TipoUsuario rol);

    Usuario buscarUsuario(String id);

    Usuario buscarUsuarioPorNombreUsuario(String nombreUsuario);

    Usuario modificarUsuario(String id, String nombreUsuario, String clave, TipoUsuario rol);

    void eliminarUsuario(String id);

    List<Usuario> listarUsuario();

    List<Usuario> listarUsuarioActivo();

}
