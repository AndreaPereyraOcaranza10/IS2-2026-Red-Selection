package redSelection.dulceMayor.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.ClaveRestablecidaDTO;
import redSelection.dulceMayor.dto.UsuarioInternoCrearDTO;
import redSelection.dulceMayor.dto.UsuarioInternoRespuestaDTO;
import redSelection.dulceMayor.enums.ProveedorAuth;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Empleado;
import redSelection.dulceMayor.model.Preventista;
import redSelection.dulceMayor.model.Transportista;
import redSelection.dulceMayor.model.Usuario;
import redSelection.dulceMayor.repository.EmpleadoRepository;
import redSelection.dulceMayor.repository.PersonaRepository;
import redSelection.dulceMayor.repository.UsuarioRepository;

import java.util.List;

/** Alta y mantenimiento de las cuentas del personal. Solo el Jefe accede a este servicio. */
@Service
public class UsuarioInternoService extends BaseService<Empleado, UsuarioInternoCrearDTO, UsuarioInternoRespuestaDTO> {

    private final EmpleadoRepository empleadoRepository;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioInternoService(EmpleadoRepository empleadoRepository,
                                 PersonaRepository personaRepository,
                                 UsuarioRepository usuarioRepository,
                                 PasswordEncoder passwordEncoder) {
        super(empleadoRepository);
        this.empleadoRepository = empleadoRepository;
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected String nombreRecurso() {
        return "Usuario interno";
    }

    @Override
    protected Empleado aEntidad(UsuarioInternoCrearDTO dto) {
        Usuario usuario = Usuario.builder()
                .nombreUsuario(dto.nombreUsuario().trim())
                .clave(passwordEncoder.encode(AuthService.CLAVE_INICIAL))
                .rol(dto.rol())
                .proveedorAuth(ProveedorAuth.LOCAL)
                .primerIngreso(true)
                .activo(true)
                .build();

        return switch (dto.rol()) {
            case PREVENTISTA -> Preventista.builder()
                    .nombre(dto.nombre().trim())
                    .apellido(dto.apellido().trim())
                    .tipoDocumento(dto.tipoDocumento())
                    .numeroDocumento(dto.numeroDocumento().trim().toUpperCase())
                    .zonaAsignada(dto.zonaAsignada().trim())
                    .usuario(usuario)
                    .build();
            case TRANSPORTISTA -> Transportista.builder()
                    .nombre(dto.nombre().trim())
                    .apellido(dto.apellido().trim())
                    .tipoDocumento(dto.tipoDocumento())
                    .numeroDocumento(dto.numeroDocumento().trim().toUpperCase())
                    .patenteVehiculo(dto.patenteVehiculo().trim().toUpperCase())
                    .usuario(usuario)
                    .build();
            case ADMINISTRATIVO -> Empleado.builder()
                    .nombre(dto.nombre().trim())
                    .apellido(dto.apellido().trim())
                    .tipoDocumento(dto.tipoDocumento())
                    .numeroDocumento(dto.numeroDocumento().trim().toUpperCase())
                    .usuario(usuario)
                    .build();
            default -> throw new ReglaNegocioException(
                    "Solo se pueden crear usuarios preventistas, transportistas o administrativos.");
        };
    }

    @Override
    protected void validarCreacion(UsuarioInternoCrearDTO dto) {
        if (dto.rol() != Rol.PREVENTISTA && dto.rol() != Rol.TRANSPORTISTA && dto.rol() != Rol.ADMINISTRATIVO) {
            throw new ReglaNegocioException(
                    "Solo se pueden crear usuarios preventistas, transportistas o administrativos.");
        }
        if (usuarioRepository.findByNombreUsuarioIgnoreCase(dto.nombreUsuario().trim()).isPresent()) {
            throw new ReglaNegocioException("Ya existe una cuenta con ese nombre de usuario.");
        }
        if (personaRepository.existsByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(
                dto.tipoDocumento(), dto.numeroDocumento().trim().toUpperCase())) {
            throw new ReglaNegocioException("Ya existe una persona con ese tipo y número de documento.");
        }
        if (dto.rol() == Rol.PREVENTISTA && (dto.zonaAsignada() == null || dto.zonaAsignada().isBlank())) {
            throw new ReglaNegocioException("Indicá la zona asignada al preventista.");
        }
        if (dto.rol() == Rol.TRANSPORTISTA && (dto.patenteVehiculo() == null || dto.patenteVehiculo().isBlank())) {
            throw new ReglaNegocioException("Indicá la patente del vehículo del transportista.");
        }
    }

    @Override
    protected void validarActualizacion(Empleado empleado, UsuarioInternoCrearDTO dto) {
        throw new ReglaNegocioException("Los datos del usuario interno no se pueden modificar desde este servicio.");
    }

    @Override
    protected void aplicarCambios(Empleado empleado, UsuarioInternoCrearDTO dto) {
        // validarActualizacion impide modificar por PUT; el método existe por el contrato de BaseService.
    }

    @Override
    protected void validarBaja(Empleado empleado) {
        if (empleado.getUsuario() == null) {
            throw new ReglaNegocioException("El empleado no tiene una cuenta asociada.");
        }
        if (empleado.getUsuario().getRol() == Rol.JEFE_ADMINISTRATIVO) {
            throw new ReglaNegocioException("La cuenta del Jefe Administrativo no se puede dar de baja desde este servicio.");
        }
    }

    @Override
    protected void ejecutarBaja(Empleado empleado) {
        empleado.setEliminado(true);
        Usuario usuario = empleado.getUsuario();
        usuario.setEliminado(true);
        usuario.setActivo(false);
        usuario.revocarTokens();
        usuarioRepository.save(usuario);
        empleadoRepository.save(empleado);
    }

    @Override
    protected List<Empleado> buscarTodos() {
        return super.buscarTodos().stream()
                .filter(empleado -> empleado.getUsuario() != null)
                .toList();
    }

    @Override
    protected UsuarioInternoRespuestaDTO aRespuesta(Empleado empleado) {
        Usuario usuario = empleado.getUsuario();
        String documento = empleado.getNumeroDocumento();
        String documentoParcial = documento == null || documento.length() <= 3
                ? "***" : "*".repeat(documento.length() - 3) + documento.substring(documento.length() - 3);
        return new UsuarioInternoRespuestaDTO(
                empleado.getId(), usuario.getNombreUsuario(), empleado.getNombre(), empleado.getApellido(),
                empleado.getTipoDocumento(), documentoParcial, usuario.getRol(), usuario.isActivo(),
                usuario.isPrimerIngreso());
    }

    @Transactional
    public ClaveRestablecidaDTO reiniciarClave(String id) {
        Empleado empleado = buscarEntidad(id);
        Usuario usuario = empleado.getUsuario();
        if (usuario == null || !usuario.isActivo() || usuario.isEliminado()) {
            throw new ReglaNegocioException("No se puede restablecer la clave de una cuenta inactiva.");
        }
        usuario.setClave(passwordEncoder.encode(AuthService.CLAVE_INICIAL));
        usuario.setPrimerIngreso(true);
        usuario.registrarAcierto();
        usuario.revocarTokens();
        usuarioRepository.save(usuario);
        return new ClaveRestablecidaDTO(
                "La clave volvió a la clave inicial. El usuario deberá cambiarla al ingresar.");
    }
}
