package redSelection.dulceMayor.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.repository.ParadaRutaRepository;
import redSelection.dulceMayor.repository.RutaDiariaRepository;

/**
 * Control de propiedad: el rol no alcanza, la parada o la ruta tienen que ser del empleado autenticado.
 * Si el recurso no existe no se responde acá: lo resuelve el servicio con su 404 habitual.
 */
@Component
public class ControlDePropiedad {

    private final ParadaRutaRepository paradaRepository;
    private final RutaDiariaRepository rutaRepository;

    public ControlDePropiedad(ParadaRutaRepository paradaRepository, RutaDiariaRepository rutaRepository) {
        this.paradaRepository = paradaRepository;
        this.rutaRepository = rutaRepository;
    }

    @Transactional(readOnly = true)
    public void exigirParada(UsuarioAutenticado usuario, String paradaId) {
        paradaRepository.findByIdAndEliminadoFalse(paradaId)
                .ifPresent(parada -> verificar(usuario, parada.getRuta().getResponsable().getId()));
    }

    @Transactional(readOnly = true)
    public void exigirRuta(UsuarioAutenticado usuario, String rutaId) {
        rutaRepository.findByIdAndEliminadoFalse(rutaId)
                .ifPresent(ruta -> verificar(usuario, ruta.getResponsable().getId()));
    }

    private static void verificar(UsuarioAutenticado usuario, String responsableId) {
        if (usuario == null || usuario.getEmpleadoId() == null || !usuario.getEmpleadoId().equals(responsableId)) {
            throw new AccessDeniedException("El recurso pertenece a otro empleado");
        }
    }
}
