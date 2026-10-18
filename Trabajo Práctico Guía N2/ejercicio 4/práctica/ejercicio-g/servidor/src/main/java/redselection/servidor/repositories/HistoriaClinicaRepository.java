package redselection.servidor.repositories;

import org.springframework.stereotype.Repository;
import redselection.servidor.model.HistoriaClinica;

import java.util.List;

@Repository
public interface HistoriaClinicaRepository extends BaseRepository<HistoriaClinica, String> {
    List<HistoriaClinica> findByEliminadoFalse();
    List<HistoriaClinica> findByPacienteIdAndEliminadoFalse(String pacienteId);
}
