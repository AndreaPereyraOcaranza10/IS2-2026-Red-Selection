package redselection.servidor.repositories;

import org.springframework.stereotype.Repository;
import redselection.servidor.model.Paciente;

import java.util.List;

@Repository
public interface PacienteRepository extends BaseRepository<Paciente, String> {
    List<Paciente> findByEliminadoFalse();
}
