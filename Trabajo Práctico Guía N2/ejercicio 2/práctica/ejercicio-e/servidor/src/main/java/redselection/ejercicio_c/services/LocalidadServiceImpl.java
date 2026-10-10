package redselection.ejercicio_c.services;

import org.springframework.stereotype.Service;
import redselection.ejercicio_c.entities.Localidad;
import redselection.ejercicio_c.repositories.BaseRepository;

@Service
public class LocalidadServiceImpl extends BaseServiceImpl<Localidad, Long> implements LocalidadService {
    public LocalidadServiceImpl(BaseRepository<Localidad, Long> baseRepository) {
        super(baseRepository);
    }
}

