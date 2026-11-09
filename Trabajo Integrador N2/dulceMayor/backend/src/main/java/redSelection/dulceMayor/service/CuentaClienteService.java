package redSelection.dulceMayor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import redSelection.dulceMayor.enums.ProveedorAuth;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.ResponsableComercio;
import redSelection.dulceMayor.model.Usuario;
import redSelection.dulceMayor.patrones.facade.NotificacionFacade;
import redSelection.dulceMayor.repository.UsuarioRepository;
import redSelection.dulceMayor.security.GeneradorClaveTemporal;

import java.util.Locale;

/**
 * Cuentas de acceso de los clientes minoristas (rol CLIENTE).
 * Hay dos caminos y los dos terminan en un Usuario:
 *  1) el comercio lo da de alta un preventista/administración: se le crea la cuenta con una CLAVE TEMPORAL
 *     aleatoria que se le manda por correo, y el usuario nace con primerIngreso = true (cambio obligatorio);
 *  2) el cliente se registra solo desde el sitio institucional, eligiendo su propia clave.
 * El usuario de acceso es el correo del responsable, que por eso no se puede modificar después.
 * No extiende BaseService: no es un CRUD, son casos de uso de acceso.
 */
@Service
public class CuentaClienteService {

    private static final Logger log = LoggerFactory.getLogger(CuentaClienteService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificacionFacade notificaciones;

    public CuentaClienteService(UsuarioRepository usuarioRepository,
                                PasswordEncoder passwordEncoder,
                                NotificacionFacade notificaciones) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificaciones = notificaciones;
    }

    // ---------- Camino 2: el cliente se registra solo ----------

    /** @return el correo normalizado con el que quedó registrada la cuenta. */
    @Transactional
    public String registrar(String email, String password) {
        String correo = normalizar(email);
        exigirCorreoLibre(correo);
        usuarioRepository.save(Usuario.builder()
                .nombreUsuario(correo)
                .clave(passwordEncoder.encode(password))
                .rol(Rol.CLIENTE)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .activo(true)
                .primerIngreso(false)   // eligió su propia clave: no hay clave inicial que cambiar
                .eliminado(false)
                .build());
        return correo;
    }

    // ---------- Camino 1: alta del comercio con clave temporal ----------

    /** Falla (422) si ya hay una cuenta con ese correo. Se llama ANTES de guardar el responsable. */
    @Transactional(readOnly = true)
    public void exigirCorreoLibre(String correo) {
        if (usuarioRepository.findByNombreUsuarioIgnoreCase(normalizar(correo)).isPresent()) {
            throw new ReglaNegocioException(
                    "Ya existe una cuenta del portal con ese correo. Usá otro correo o pedí ayuda a administración.");
        }
    }

    /**
     * Crea la cuenta del responsable y le manda la clave temporal. El correo sale DESPUÉS del commit: si el alta
     * falla y se deshace, no queda un mail avisando de una cuenta que no existe. Si el correo no sale, el alta
     * igual queda hecha y administración puede reenviar el acceso (reenviarAcceso).
     */
    @Transactional
    public void crearCuentaConClaveTemporal(ResponsableComercio responsable, String correo) {
        String clave = GeneradorClaveTemporal.generar();
        Usuario usuario = guardarUsuarioTemporal(normalizar(correo), clave);
        responsable.setUsuario(usuario);
        String nombre = nombreCompleto(responsable);
        enviarDespuesDelCommit(usuario.getNombreUsuario(), nombre, usuario.getNombreUsuario(), clave);
    }

    /**
     * Administración reenvía el acceso: genera una clave temporal nueva, obliga a cambiarla, cierra las sesiones
     * abiertas y levanta un bloqueo por intentos fallidos. Si el responsable no tenía cuenta, se la crea.
     * Acá el correo sale DENTRO de la transacción: si no se puede enviar, se deshace todo (la clave anterior sigue
     * valiendo) y administración ve el error en vez de creer que el cliente recibió algo.
     */
    @Transactional
    public void reenviarAcceso(ResponsableComercio responsable, String correoContacto) {
        String clave = GeneradorClaveTemporal.generar();
        Usuario usuario = responsable.getUsuario();
        if (usuario == null) {
            String login = normalizar(correoContacto);
            exigirCorreoLibre(login);
            usuario = guardarUsuarioTemporal(login, clave);
            responsable.setUsuario(usuario);
        } else {
            usuario.setClave(passwordEncoder.encode(clave));
            usuario.setPrimerIngreso(true);
            usuario.revocarTokens();
            usuario.registrarAcierto();
            usuarioRepository.save(usuario);
        }
        if (!notificaciones.enviarClaveTemporal(correoContacto, nombreCompleto(responsable),
                usuario.getNombreUsuario(), clave)) {
            throw new ReglaNegocioException("No se pudo enviar el correo. Revisá la configuración de correo e intentá de nuevo.");
        }
    }

    // ---------- Consulta para el perfil ----------

    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioActivo(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuarioIgnoreCase(nombreUsuario == null ? "" : nombreUsuario.trim())
                .filter(u -> u.isActivo() && !u.isEliminado())
                .orElseThrow(() -> new ReglaNegocioException("No encontramos tu cuenta. Volvé a iniciar sesión."));
    }

    // ---------- Auxiliares ----------

    private Usuario guardarUsuarioTemporal(String login, String clave) {
        return usuarioRepository.save(Usuario.builder()
                .nombreUsuario(login)
                .clave(passwordEncoder.encode(clave))   // BCrypt: la clave en claro no se guarda en ningún lado
                .rol(Rol.CLIENTE)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .activo(true)
                .primerIngreso(true)                    // el backend obliga a cambiarla antes de seguir
                .eliminado(false)
                .build());
    }

    private void enviarDespuesDelCommit(String correo, String nombre, String usuario, String clave) {
        Runnable envio = () -> {
            try {
                if (!notificaciones.enviarClaveTemporal(correo, nombre, usuario, clave)) {
                    log.warn("No se pudo enviar el correo de acceso al usuario {} (administración puede reenviarlo).", usuario);
                }
            } catch (RuntimeException e) {
                log.warn("Falló el envío del correo de acceso al usuario {}: {}", usuario, e.getMessage());
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { envio.run(); }
            });
        } else {
            envio.run();
        }
    }

    private static String nombreCompleto(ResponsableComercio r) {
        return r.getNombre() + " " + r.getApellido();
    }

    private static String normalizar(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }
}