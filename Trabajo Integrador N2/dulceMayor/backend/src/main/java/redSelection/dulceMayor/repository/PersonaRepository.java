package redSelection.dulceMayor.repository;

import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.enums.TipoDocumento;
import redSelection.dulceMayor.model.Persona;

import java.util.Optional;

@Repository
public interface PersonaRepository extends BaseRepository<Persona> {
    Optional<Persona> findByUsuarioId(String usuarioId);
    boolean existsByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(TipoDocumento tipoDocumento, String numeroDocumento);
}
