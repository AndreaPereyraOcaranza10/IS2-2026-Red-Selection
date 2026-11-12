// TransportistaService.java
package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.model.Transportista;
import redSelection.dulceMayor.repository.TransportistaRepository;

import java.util.List;

/** Acceso a transportistas para el resto de los services (nadie usa TransportistaRepository ajeno). */
@Service
public class TransportistaService {

    private final TransportistaRepository transportistaRepository;

    public TransportistaService(TransportistaRepository transportistaRepository) {
        this.transportistaRepository = transportistaRepository;
    }

    @Transactional(readOnly = true)
    public Transportista obtenerEntidad(String id) {
        return transportistaRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Transportista", id));
    }

    /** Transportistas que pueden recibir pedidos: no dados de baja y con usuario activo. */
    @Transactional(readOnly = true)
    public List<Transportista> activos() {
        return transportistaRepository.findByEliminadoFalse().stream().filter(this::estaActivo).toList();
    }

    public boolean estaActivo(Transportista transportista) {
        return transportista.getUsuario() != null && transportista.getUsuario().isActivo();
    }
}