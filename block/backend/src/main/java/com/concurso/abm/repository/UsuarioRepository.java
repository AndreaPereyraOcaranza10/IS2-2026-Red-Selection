package com.concurso.abm.repository;

import com.concurso.abm.entity.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  Optional<Usuario> findByEmail(String email);

  Optional<Usuario> findByProveedorAutenticacionAndProveedorId(String provider, String id);

  boolean existsByPersonaIdAndActivoTrue(Long id);
}
