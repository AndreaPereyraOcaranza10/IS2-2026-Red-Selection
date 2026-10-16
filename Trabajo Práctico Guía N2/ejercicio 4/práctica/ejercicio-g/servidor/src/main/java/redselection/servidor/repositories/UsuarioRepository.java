package redselection.servidor.repositories;

import org.springframework.stereotype.Repository;
import redselection.servidor.model.Usuario;

import java.util.List;

@Repository
public interface UsuarioRepository extends BaseRepository<Usuario, String> {
    List<Usuario> findByEliminadoFalse();
}