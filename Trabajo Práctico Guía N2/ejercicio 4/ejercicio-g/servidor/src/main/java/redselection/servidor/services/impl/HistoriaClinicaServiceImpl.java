package redselection.servidor.services.impl;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import redselection.servidor.model.DetalleHistoriaClinica;
import redselection.servidor.model.HistoriaClinica;
import redselection.servidor.repositories.HistoriaClinicaRepository;
import redselection.servidor.services.HistoriaClinicaService;

import java.util.List;

@Service
public class HistoriaClinicaServiceImpl extends BaseServiceImpl<HistoriaClinica> implements HistoriaClinicaService {

    private final HistoriaClinicaRepository historiaRepository;

    public HistoriaClinicaServiceImpl(HistoriaClinicaRepository repository) {
        super(repository);
        this.historiaRepository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriaClinica> findByPaciente(String pacienteId) {
        return historiaRepository.findByPacienteIdAndEliminadoFalse(pacienteId);
    }

    @Override
    protected void validar(HistoriaClinica h) {
        if (h.getPaciente() == null || h.getPaciente().getId() == null)
            throw new IllegalArgumentException("La historia clínica debe tener un paciente");
        if (h.getUsuario() == null || h.getUsuario().getId() == null)
            throw new IllegalArgumentException("La historia clínica debe tener un usuario");

        for (DetalleHistoriaClinica d : h.getDetalles()) {
            if (d.getFechaHistoria() == null)
                throw new IllegalArgumentException("Cada detalle necesita una fecha");
            if (d.getMedico() == null || d.getMedico().getId() == null)
                throw new IllegalArgumentException("Cada detalle necesita un médico");
        }
    }

    // Hook: como en DetalleHistoriaClinica la referencia a la historia tiene @JsonIgnore,
    // al llegar el JSON viene en null. Hay que reasignarla a mano antes de guardar.
    @Override
    protected void antesDeGuardar(HistoriaClinica h) {
        vincularDetalles(h);
    }

    @Override
    protected void antesDeActualizar(HistoriaClinica h) {
        vincularDetalles(h);
    }

    private void vincularDetalles(HistoriaClinica h) {
        for (DetalleHistoriaClinica d : h.getDetalles()) {
            d.setId(null);
            d.setEliminado(false);
            d.setHistoriaClinica(h);
        }
    }
}
