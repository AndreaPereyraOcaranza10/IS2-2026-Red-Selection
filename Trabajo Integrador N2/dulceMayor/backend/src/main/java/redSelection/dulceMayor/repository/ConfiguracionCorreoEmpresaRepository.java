package redSelection.dulceMayor.repository;

import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.enums.TipoCorreo;
import redSelection.dulceMayor.model.ConfiguracionCorreoEmpresa;

import java.util.Optional;

@Repository
public interface ConfiguracionCorreoEmpresaRepository extends BaseRepository<ConfiguracionCorreoEmpresa> {
    Optional<ConfiguracionCorreoEmpresa> findByDulceMayorIdAndTipoCorreo(String dulceMayorId, TipoCorreo tipoCorreo);
}
