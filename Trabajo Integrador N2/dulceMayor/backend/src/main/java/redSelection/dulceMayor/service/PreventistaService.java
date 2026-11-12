package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.model.Preventista;
import redSelection.dulceMayor.repository.PreventistaRepository;
import java.util.Optional;

/* Acceso a preventistas para el resto de los services (nadie usa PreventistaRepository ajeno). */
@Service
public class PreventistaService {

    private final PreventistaRepository preventistaRepository;

    public PreventistaService(PreventistaRepository preventistaRepository) {
        this.preventistaRepository = preventistaRepository;
    }

    /** Cualquier preventista activo: PedidoService se lo asigna a los pedidos que carga administración sin preventista propio. */
    @Transactional(readOnly = true)
    public Optional<Preventista> primerActivo() {
        return preventistaRepository.findByEliminadoFalse().stream().findFirst();
    }

    @Transactional(readOnly = true)
    public Preventista obtenerEntidad(String id) {
        return preventistaRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Preventista", id));
    }

    /** Para EstadisticasService: todos los preventistas activos. */
    @Transactional(readOnly = true)
    public java.util.List<Preventista> activos() {
        return preventistaRepository.findByEliminadoFalse();
    }

}