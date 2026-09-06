package com.RedSelection.videojuegos.repositories;

import com.RedSelection.videojuegos.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

    @Query("SELECT u "
            + "  FROM Usuario u "
            + " WHERE u.email = :email "
            + "   AND u.eliminado = FALSE")
    public Usuario buscarUsuarioPorEmail(@Param("email") String email);
}
