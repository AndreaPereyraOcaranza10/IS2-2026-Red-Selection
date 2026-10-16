package redselection.servidor.services.impl;

import org.springframework.stereotype.Service;
import redselection.servidor.model.Paciente;
import redselection.servidor.repositories.PacienteRepository;
import redselection.servidor.services.PacienteService;

import java.util.List;

@Service
public class PacienteServiceImpl extends BaseServiceImpl<Paciente> implements PacienteService {

    public PacienteServiceImpl(PacienteRepository repository) {
        super(repository);
    }

    @Override
    protected void validar(Paciente p) {
        if (p.getNombre() == null || p.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre del paciente es obligatorio");
        if (p.getApellido() == null || p.getApellido().isBlank())
            throw new IllegalArgumentException("El apellido del paciente es obligatorio");
        if (p.getDocumento() == null || p.getDocumento().isBlank())
            throw new IllegalArgumentException("El documento del paciente es obligatorio");
    }
}
