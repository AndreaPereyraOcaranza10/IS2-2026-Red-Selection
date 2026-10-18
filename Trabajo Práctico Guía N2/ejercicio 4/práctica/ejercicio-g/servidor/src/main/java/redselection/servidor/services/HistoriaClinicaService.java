package redselection.servidor.services;

import redselection.servidor.model.HistoriaClinica;

import java.util.List;

public interface HistoriaClinicaService extends BaseService<HistoriaClinica> {
    List<HistoriaClinica> findByPaciente(String pacienteId);
}