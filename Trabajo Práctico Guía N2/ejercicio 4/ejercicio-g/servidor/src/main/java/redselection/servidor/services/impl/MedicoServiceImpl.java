package redselection.servidor.services.impl;

import org.springframework.stereotype.Service;
import redselection.servidor.model.Medico;
import redselection.servidor.repositories.MedicoRepository;
import redselection.servidor.services.MedicoService;

import java.util.List;

@Service
public class MedicoServiceImpl extends BaseServiceImpl<Medico> implements MedicoService {

    public MedicoServiceImpl(MedicoRepository repository) {
        super(repository);
    }

    @Override
    protected void validar(Medico m) {
        if (m.getNombre() == null || m.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre del médico es obligatorio");
        if (m.getApellido() == null || m.getApellido().isBlank())
            throw new IllegalArgumentException("El apellido del médico es obligatorio");
        if (m.getDocumento() == null || m.getDocumento().isBlank())
            throw new IllegalArgumentException("El documento del médico es obligatorio");
    }
}