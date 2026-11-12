package redSelection.dulceMayor.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.PuntoVentaCrearDTO;
import redSelection.dulceMayor.dto.PuntoVentaRespuestaDTO;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.ClienteMinorista;
import redSelection.dulceMayor.model.Preventista;
import redSelection.dulceMayor.model.ResponsableComercio;
import redSelection.dulceMayor.repository.ClienteMinoristaRepository;
import redSelection.dulceMayor.security.UsuarioActual;
import redSelection.dulceMayor.security.UsuarioAutenticado;

import java.util.List;

/**
 * PATRÓN: Template Method (capa Service). Un punto de venta es un ClienteMinorista.
 * - El alta lo asigna al preventista logueado y, si ya tiene ruta armada hoy, lo suma como parada pendiente.
 * - Control de propiedad: un preventista solo lista, consulta y modifica los suyos (el administrativo, todos).
 */
@Service
public class PuntoVentaService extends BaseService<ClienteMinorista, PuntoVentaCrearDTO, PuntoVentaRespuestaDTO> {

    private final ClienteMinoristaRepository clienteRepository;
    private final DireccionService direccionService;
    private final ResponsableComercioService responsableService;
    private final PreventistaService preventistaService;
    private final ApplicationEventPublisher eventos;
    private final UsuarioActual usuarioActual;

    public PuntoVentaService(ClienteMinoristaRepository clienteRepository,
                             DireccionService direccionService,
                             ResponsableComercioService responsableService,
                             PreventistaService preventistaService,
                             ApplicationEventPublisher eventos,        // antes: RutaService rutaService,
                             UsuarioActual usuarioActual) {
        super(clienteRepository);
        this.clienteRepository = clienteRepository;
        this.direccionService = direccionService;
        this.responsableService = responsableService;
        this.preventistaService = preventistaService;
        this.eventos = eventos;                                        // antes: this.rutaService = rutaService;
        this.usuarioActual = usuarioActual;
    }
    // ---------- Pasos del Template Method ----------

    @Override protected String nombreRecurso() { return "Punto de venta"; }

    @Override
    protected ClienteMinorista aEntidad(PuntoVentaCrearDTO dto) {
        String nombre = dto.nombreComercio().trim();
        return ClienteMinorista.builder()
                .nombreComercio(nombre)
                .razonSocial(razonSocial(dto, nombre))
                .cuit(cuitConGuiones(dto.cuit()))
                .direccion(direccionService.crear(dto.direccion()))
                .responsable(resolverResponsable(dto))
                .preventistaAsignado(preventistaActual())
                .build();
    }

    @Override
    protected void aplicarCambios(ClienteMinorista cliente, PuntoVentaCrearDTO dto) {
        String nombre = dto.nombreComercio().trim();
        cliente.setNombreComercio(nombre);
        cliente.setRazonSocial(razonSocial(dto, nombre));
        cliente.setCuit(cuitConGuiones(dto.cuit()));
        direccionService.actualizar(cliente.getDireccion(), dto.direccion());
        cliente.setResponsable(resolverResponsable(dto));
    }

    @Override
    protected PuntoVentaRespuestaDTO aRespuesta(ClienteMinorista c) {
        return new PuntoVentaRespuestaDTO(
                c.getId(), c.getNombreComercio(), c.getRazonSocial(), c.getCuit(),
                direccionService.aRespuesta(c.getDireccion()),
                responsableService.aRespuesta(c.getResponsable()),
                c.getPreventistaAsignado() != null ? c.getPreventistaAsignado().getId() : null);
    }

    // ---------- Hooks ----------

    @Override
    protected void validarCreacion(PuntoVentaCrearDTO dto) {
        validarResponsableUnico(dto);
        if (clienteRepository.existsByCuitAndEliminadoFalse(cuitConGuiones(dto.cuit()))) {
            throw new ReglaNegocioException("Ya existe un comercio con ese CUIT.");
        }
    }

    @Override
    protected void validarActualizacion(ClienteMinorista cliente, PuntoVentaCrearDTO dto) {
        exigirPropiedad(cliente);
        validarResponsableUnico(dto);
        if (clienteRepository.existsByCuitAndIdNotAndEliminadoFalse(cuitConGuiones(dto.cuit()), cliente.getId())) {
            throw new ReglaNegocioException("Ya existe otro comercio con ese CUIT.");
        }
    }

    @Override
    protected void validarBaja(ClienteMinorista cliente) {
        exigirPropiedad(cliente);
    }

    /**
     * El punto de venta ya está guardado: se avisa (Observer) y RutaService lo suma como parada pendiente
     * si el preventista ya tiene ruta armada hoy.
     */
    @Override
    protected void despuesDeCrear(ClienteMinorista cliente, PuntoVentaCrearDTO dto) {
        if (cliente.getPreventistaAsignado() != null) {
            eventos.publishEvent(new PuntoVentaAltaEvento(cliente.getPreventistaAsignado().getId(), cliente));
        }
    }

    /** Para otros services (la ruta de preventa): los comercios asignados a un preventista, sin control de acceso. */
    @Transactional(readOnly = true)
    public List<ClienteMinorista> clientesAsignados(String preventistaId) {
        return clienteRepository.findByPreventistaAsignadoIdAndEliminadoFalse(preventistaId);
    }

    // ---------- Control de propiedad en las lecturas ----------

    @Override
    @Transactional(readOnly = true)
    public PuntoVentaRespuestaDTO obtener(String id) {
        ClienteMinorista cliente = buscarEntidad(id);
        exigirPropiedad(cliente);
        return aRespuesta(cliente);
    }

    @Override
    protected List<ClienteMinorista> buscarTodos() {
        UsuarioAutenticado usuario = usuarioActual.obtener();
        if (usuario.getRol() == Rol.PREVENTISTA) {
            return clienteRepository.findByPreventistaAsignadoIdAndEliminadoFalse(empleadoIdObligatorio(usuario));
        }
        return super.buscarTodos();
    }

    // ---------- Auxiliares ----------

    /** Exactamente una de las dos formas de indicar el responsable. */
    private void validarResponsableUnico(PuntoVentaCrearDTO dto) {
        boolean existente = dto.responsableId() != null && !dto.responsableId().isBlank();
        boolean nuevo = dto.responsableNuevo() != null;
        if (existente == nuevo) {
            throw new ReglaNegocioException(
                    "Elegí un responsable ya cargado o completá los datos de uno nuevo (una sola opción).");
        }
    }

    private ResponsableComercio resolverResponsable(PuntoVentaCrearDTO dto) {
        if (dto.responsableNuevo() != null) {
            return responsableService.crearEntidad(dto.responsableNuevo());
        }
        return responsableService.obtenerEntidad(dto.responsableId());   // 404 si no existe
    }

    /** El preventista logueado; el administrativo da de alta comercios sin preventista (se le asignan después). */
    private Preventista preventistaActual() {
        UsuarioAutenticado usuario = usuarioActual.obtener();
        if (usuario.getRol() != Rol.PREVENTISTA) {
            return null;
        }
        return preventistaService.obtenerEntidad(empleadoIdObligatorio(usuario));
    }

    private void exigirPropiedad(ClienteMinorista cliente) {
        UsuarioAutenticado usuario = usuarioActual.obtener();
        if (usuario.getRol().esAdministracion()) {
            return;
        }
        Preventista dueno = cliente.getPreventistaAsignado();
        if (usuario.getRol() != Rol.PREVENTISTA || dueno == null || !dueno.getId().equals(usuario.getEmpleadoId())) {
            throw new AccessDeniedException("El punto de venta pertenece a otro preventista");
        }
    }

    private static String empleadoIdObligatorio(UsuarioAutenticado usuario) {
        if (usuario.getEmpleadoId() == null) {
            throw new AccessDeniedException("Tu usuario no tiene un empleado asociado");
        }
        return usuario.getEmpleadoId();
    }

    private static String razonSocial(PuntoVentaCrearDTO dto, String nombreComercio) {
        return dto.razonSocial() == null || dto.razonSocial().isBlank() ? nombreComercio : dto.razonSocial().trim();
    }

    /** Se guarda siempre como XX-XXXXXXXX-X (igual que los datos de demostración) para que la unicidad sea consistente. */
    private static String cuitConGuiones(String cuit) {
        String d = cuit.replaceAll("\\D", "");
        return d.substring(0, 2) + "-" + d.substring(2, 10) + "-" + d.substring(10);
    }
}
