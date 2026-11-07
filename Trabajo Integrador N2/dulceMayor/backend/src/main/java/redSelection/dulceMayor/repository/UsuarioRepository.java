package redSelection.dulceMayor.repository;

import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.enums.ProveedorAuth;
import redSelection.dulceMayor.model.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends BaseRepository<Usuario> {
    Optional<Usuario> findByNombreUsuarioIgnoreCase(String nombreUsuario);
    Optional<Usuario> findByProveedorAuthAndIdExterno(ProveedorAuth proveedorAuth, String idExterno);
}
