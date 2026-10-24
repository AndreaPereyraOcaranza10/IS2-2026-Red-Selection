package redselection.ejercicio_c.repositories;

import org.springframework.stereotype.Repository;
import redselection.ejercicio_c.entities.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends BaseRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
}
