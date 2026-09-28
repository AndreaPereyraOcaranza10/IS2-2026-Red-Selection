package com.tienda.zero.repository;

import com.tienda.zero.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Usuario.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    /**
     * Busca un usuario por su correo electrónico (insensible a mayúsculas/minúsculas).
     */
    Optional<Usuario> findByEmailIgnoreCase(String email);

    /**
     * Busca un usuario por su código de activación.
     */
    Optional<Usuario> findByCodigoActivacion(String codigoActivacion);

    /**
     * Comprueba si ya existe un usuario registrado con el correo indicado.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Lista todos los usuarios activos no eliminados.
     */
    List<Usuario> findByEliminadoFalse();
}
