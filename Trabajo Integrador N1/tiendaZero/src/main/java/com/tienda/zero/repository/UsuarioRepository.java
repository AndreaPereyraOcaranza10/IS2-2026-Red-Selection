package com.tienda.zero.repository;

import com.tienda.zero.model.Usuario;
import com.tienda.zero.enums.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    List<Usuario> findByEliminadoFalse();
    List<Usuario> findByRolAndCuentaActivadaTrueAndEliminadoFalse(TipoUsuario rol);
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

}
