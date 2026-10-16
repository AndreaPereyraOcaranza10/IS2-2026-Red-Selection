package redselection.servidor.repositories;

import org.springframework.stereotype.Repository;
import redselection.servidor.model.Medico;

import java.util.List;

@Repository
public interface MedicoRepository extends BaseRepository<Medico, String> {
    List<Medico> findByEliminadoFalse();
}
