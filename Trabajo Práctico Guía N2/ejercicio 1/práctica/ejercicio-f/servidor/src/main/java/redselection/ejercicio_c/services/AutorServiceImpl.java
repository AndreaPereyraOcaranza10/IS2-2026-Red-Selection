package redselection.ejercicio_c.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redselection.ejercicio_c.dto.AutorDTO;
import redselection.ejercicio_c.entities.Autor;
import redselection.ejercicio_c.mappers.AutorMapper;
import redselection.ejercicio_c.repositories.AutorRepository;
import redselection.ejercicio_c.repositories.BaseRepository;

@Service
public class AutorServiceImpl extends BaseServiceImpl<Autor, AutorDTO, Long> implements AutorService {

    @Autowired
    private AutorRepository autorRepository;

    public AutorServiceImpl(BaseRepository<Autor, Long> baseRepository, AutorMapper autorMapper) {
        super(baseRepository, autorMapper);
    }
}

