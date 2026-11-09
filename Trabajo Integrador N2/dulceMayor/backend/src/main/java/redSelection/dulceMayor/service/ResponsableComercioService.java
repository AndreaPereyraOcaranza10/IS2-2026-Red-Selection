package redSelection.dulceMayor.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.AccesoReenviadoDTO;
import redSelection.dulceMayor.dto.ResponsableCrearDTO;
import redSelection.dulceMayor.dto.ResponsableRespuestaDTO;
import redSelection.dulceMayor.enums.TipoContacto;
import redSelection.dulceMayor.enums.TipoDocumento;
import redSelection.dulceMayor.enums.TipoTelefono;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Contacto;
import redSelection.dulceMayor.model.ContactoCorreoElectronico;
import redSelection.dulceMayor.model.ContactoTelefonico;
import redSelection.dulceMayor.model.ResponsableComercio;
import redSelection.dulceMayor.repository.ResponsableComercioRepository;
import redSelection.dulceMayor.validacion.ValidadorPersona;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 PATRÓN: Template Method (capa Service). El correo y el teléfono se guardan como contactos principales:
 el correo es el que usa NotificacionFacade para avisar "es tu turno" al comercio.
 Al dar de alta un responsable se le crea su cuenta del portal (CuentaClienteService) con una clave temporal por
 correo; por eso el correo NO se puede modificar después: es su usuario de acceso.
 Las reglas de datos personales (nombre sin números, DNI de 8 números, teléfono de 10 u 11) están en
 ValidadorPersona y se aplican igual en el alta y en la modificación.
 */
@Service
public class ResponsableComercioService extends BaseService<ResponsableComercio, ResponsableCrearDTO, ResponsableRespuestaDTO> {

    private static final int MAXIMO_RESULTADOS = 20;

    private final ResponsableComercioRepository responsableRepository;
    private final CuentaClienteService cuentas;

    public ResponsableComercioService(ResponsableComercioRepository responsableRepository,
                                      CuentaClienteService cuentas) {
        super(responsableRepository);
        this.responsableRepository = responsableRepository;
        this.cuentas = cuentas;
    }

    // ---------- Pasos del Template Method ----------

    @Override protected String nombreRecurso() { return "Responsable"; }

    @Override
    protected ResponsableComercio aEntidad(ResponsableCrearDTO dto) {
        List<Contacto> contactos = new ArrayList<>();
        contactos.add(nuevoCorreo(dto));
        contactos.add(nuevoTelefono(dto));
        return ResponsableComercio.builder()
                .nombre(dto.nombre().trim())
                .apellido(dto.apellido().trim())
                .tipoDocumento(dto.tipoDocumento())
                .numeroDocumento(documento(dto))
                .contactos(contactos)
                .build();
    }

    /** El correo no se toca: validarActualizacion ya exigió que llegue igual al que está guardado. */
    @Override
    protected void aplicarCambios(ResponsableComercio responsable, ResponsableCrearDTO dto) {
        responsable.setNombre(dto.nombre().trim());
        responsable.setApellido(dto.apellido().trim());
        responsable.setTipoDocumento(dto.tipoDocumento());
        responsable.setNumeroDocumento(documento(dto));
        if (principal(responsable, ContactoCorreoElectronico.class).isEmpty()) {
            responsable.getContactos().add(nuevoCorreo(dto));   // dato viejo sin correo: se puede cargar una vez
        }
        principal(responsable, ContactoTelefonico.class).ifPresentOrElse(
                c -> {
                    c.setTelefono(dto.telefono().trim());
                    c.setTipoTelefono(tipoTelefono(dto));
                },
                () -> responsable.getContactos().add(nuevoTelefono(dto)));
    }

    /** Público a propósito: PuntoVentaService arma su respuesta con el responsable ya cargado. */
    @Override
    public ResponsableRespuestaDTO aRespuesta(ResponsableComercio r) {
        return new ResponsableRespuestaDTO(
                r.getId(), r.getNombre(), r.getApellido(), r.getTipoDocumento(),
                enmascarar(r.getNumeroDocumento()),
                principal(r, ContactoCorreoElectronico.class).map(ContactoCorreoElectronico::getEmail).orElse(null),
                principal(r, ContactoTelefonico.class).map(ContactoTelefonico::getTelefono).orElse(null));
    }

    // ---------- Hooks ----------

    @Override
    protected void validarCreacion(ResponsableCrearDTO dto) {
        validarDatosPersonales(dto);
        exigirDocumentoDisponible(dto.tipoDocumento(), documento(dto), null);
        cuentas.exigirCorreoLibre(correo(dto));
    }

    @Override
    protected void validarActualizacion(ResponsableComercio actual, ResponsableCrearDTO dto) {
        validarDatosPersonales(dto);
        ValidadorPersona.correoSinCambios(
                principal(actual, ContactoCorreoElectronico.class).map(ContactoCorreoElectronico::getEmail).orElse(null),
                dto.email());
        exigirDocumentoDisponible(dto.tipoDocumento(), documento(dto), actual);
    }

    /** Ya guardado el responsable: se le crea la cuenta del portal y se le manda la clave temporal. */
    @Override
    protected void despuesDeCrear(ResponsableComercio responsable, ResponsableCrearDTO dto) {
        cuentas.crearCuentaConClaveTemporal(responsable, correo(dto));
    }

    // ---------- Casos de uso propios ----------

    /** Para el selector del alta: por nombre, apellido o documento. Mínimo 2 caracteres, máximo 20 resultados. */
    @Transactional(readOnly = true)
    public List<ResponsableRespuestaDTO> buscar(String texto) {
        String limpio = texto == null ? "" : texto.replaceAll("[^\\p{L}\\d ]", "").trim();
        if (limpio.length() < 2) {
            throw new ReglaNegocioException("Escribí al menos 2 letras o números para buscar.");
        }
        return responsableRepository.buscar(limpio, PageRequest.of(0, MAXIMO_RESULTADOS))
                .stream().map(this::aRespuesta).toList();
    }

    /** Mismo esqueleto que crear(), pero devuelve la entidad: lo usa el alta de un punto de venta con responsable nuevo. */
    @Transactional
    public ResponsableComercio crearEntidad(ResponsableCrearDTO dto) {
        validarCreacion(dto);
        ResponsableComercio guardado = responsableRepository.save(aEntidad(dto));
        despuesDeCrear(guardado, dto);
        return guardado;
    }

    /**
     * Administración reenvía el acceso al portal (clave temporal nueva por correo). Sirve si el cliente nunca
     * recibió el mail, lo perdió, se olvidó la clave, o es un responsable viejo que todavía no tenía cuenta.
     */
    @Transactional
    public AccesoReenviadoDTO reenviarAcceso(String id) {
        ResponsableComercio responsable = buscarEntidad(id);
        String correo = principal(responsable, ContactoCorreoElectronico.class)
                .map(ContactoCorreoElectronico::getEmail)
                .orElseThrow(() -> new ReglaNegocioException("El responsable no tiene un correo cargado."));
        cuentas.reenviarAcceso(responsable, correo);
        return new AccesoReenviadoDTO(correo, "Le enviamos una clave temporal. Va a tener que cambiarla al ingresar.");
    }

    // ---------- Para el perfil del portal (PerfilClienteService) ----------

    /** El responsable cuya cuenta de acceso tiene ese nombre de usuario (el correo), si ya existe. */
    @Transactional(readOnly = true)
    public Optional<ResponsableComercio> buscarPorNombreUsuario(String nombreUsuario) {
        return responsableRepository.findByEliminadoFalse().stream()
                .filter(r -> r.getUsuario() != null && r.getUsuario().getNombreUsuario().equalsIgnoreCase(nombreUsuario))
                .findFirst();
    }

    @Transactional
    public ResponsableComercio guardar(ResponsableComercio responsable) {
        return responsableRepository.save(responsable);
    }

    /** Un documento no puede estar en dos responsables. `actual` es el que se está editando (null si es un alta). */
    @Transactional(readOnly = true)
    public void exigirDocumentoDisponible(TipoDocumento tipo, String numeroDocumento, ResponsableComercio actual) {
        boolean sinCambios = actual != null && actual.getTipoDocumento() == tipo
                && numeroDocumento.equals(actual.getNumeroDocumento());
        if (!sinCambios && responsableRepository.existsByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(tipo, numeroDocumento)) {
            throw new ReglaNegocioException(actual == null
                    ? "Ya existe un responsable con ese documento. Elegilo de la lista en lugar de cargarlo de nuevo."
                    : "Ya existe otro responsable con ese documento.");
        }
    }

    // ---------- Auxiliares ----------

    private static void validarDatosPersonales(ResponsableCrearDTO dto) {
        ValidadorPersona.nombre("nombre", dto.nombre());
        ValidadorPersona.nombre("apellido", dto.apellido());
        ValidadorPersona.documento(dto.tipoDocumento(), dto.numeroDocumento());
        ValidadorPersona.telefono(dto.telefono());
    }

    private static String documento(ResponsableCrearDTO dto) { return dto.numeroDocumento().trim().toUpperCase(); }
    private static String correo(ResponsableCrearDTO dto) { return dto.email().trim().toLowerCase(); }

    private static TipoTelefono tipoTelefono(ResponsableCrearDTO dto) {
        return dto.tipoTelefono() != null ? dto.tipoTelefono() : TipoTelefono.CELULAR;
    }

    private static ContactoCorreoElectronico nuevoCorreo(ResponsableCrearDTO dto) {
        return ContactoCorreoElectronico.builder()
                .tipoContacto(TipoContacto.LABORAL).principal(true).email(correo(dto)).build();
    }

    private static ContactoTelefonico nuevoTelefono(ResponsableCrearDTO dto) {
        return ContactoTelefonico.builder()
                .tipoContacto(TipoContacto.LABORAL).principal(true)
                .telefono(dto.telefono().trim()).tipoTelefono(tipoTelefono(dto)).build();
    }

    /** El contacto principal de ese tipo (o el primero que haya). */
    private static <T extends Contacto> Optional<T> principal(ResponsableComercio r, Class<T> tipo) {
        return r.getContactos().stream()
                .filter(c -> tipo.isInstance(c) && !c.isEliminado())
                .map(tipo::cast)
                .max(Comparator.comparing(Contacto::isPrincipal));
    }

    private static String enmascarar(String documento) {
        if (documento == null || documento.length() <= 3) {
            return "***";
        }
        return "*".repeat(documento.length() - 3) + documento.substring(documento.length() - 3);
    }
}