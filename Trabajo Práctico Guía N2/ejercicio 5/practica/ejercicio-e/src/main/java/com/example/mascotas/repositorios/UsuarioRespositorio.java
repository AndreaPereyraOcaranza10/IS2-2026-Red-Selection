
package com.example.mascotas.repositorios;

import com.example.mascotas.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRespositorio  extends JpaRepository<Usuario, String> {
    java.util.Optional<Usuario> findByOauth2Subject(String subject);
    @Query("SELECT c FROM Usuario c WHERE c.mail = :mail")
    public Usuario buscarPorMail(@Param("mail") String mail); 
}
