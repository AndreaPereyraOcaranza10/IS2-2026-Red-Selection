package redSelection.dulceMayor.repository;

import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.model.ClienteMinorista;

import java.util.List;

@Repository
public interface ClienteMinoristaRepository extends BaseRepository<ClienteMinorista> {
    boolean existsByCuitAndEliminadoFalse(String cuit);
    List<ClienteMinorista> findByPreventistaAsignadoIdAndEliminadoFalse(String preventistaId);
    List<ClienteMinorista> findByResponsableIdAndEliminadoFalse(String responsableId);
    boolean existsByCuitAndIdNotAndEliminadoFalse(String cuit, String id);
}
