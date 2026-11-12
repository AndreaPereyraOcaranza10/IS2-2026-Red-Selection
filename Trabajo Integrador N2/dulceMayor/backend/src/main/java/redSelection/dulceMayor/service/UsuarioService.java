// service/UsuarioService.java
package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.enums.TipoDocumento;
import redSelection.dulceMayor.model.Persona;
import redSelection.dulceMayor.model.Usuario;
import redSelection.dulceMayor.repository.PersonaRepository;
import redSelection.dulceMayor.repository.UsuarioRepository;

import java.util.Optional;

/**
 * Dueño de las cuentas de acceso (Usuario) y de la relación Usuario → Persona.
 * AuthService, CuentaClienteService y UsuarioInternoService piden y guardan usuarios acá,
 * ninguno toca UsuarioRepository ni PersonaRepository directamente.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, PersonaRepository personaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuarioIgnoreCase(nombreUsuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarActivoPorId(String id) {
        return usuarioRepository.findByIdAndEliminadoFalse(id);
    }

    @Transactional
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    /** La persona (empleado o responsable de comercio) dueña de esa cuenta, si tiene. */
    @Transactional(readOnly = true)
    public Optional<Persona> personaDe(String usuarioId) {
        return personaRepository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public boolean existePersonaConDocumento(TipoDocumento tipo, String numero) {
        return personaRepository.existsByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(tipo, numero);
    }
}