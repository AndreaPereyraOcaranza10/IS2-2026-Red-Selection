package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.model.Preventista;
import redSelection.dulceMayor.repository.PreventistaRepository;

/* Acceso a preventistas para el resto de los services (nadie usa PreventistaRepository ajeno). */
@Service
public class PreventistaService {

    private final PreventistaRepository preventistaRepository;

    public PreventistaService(PreventistaRepository preventistaRepository) {
        this.preventistaRepository = preventistaRepository;
    }

    @Transactional(readOnly = true)
    public Preventista obtenerEntidad(String id) {
        return preventistaRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Preventista", id));
    }
}
