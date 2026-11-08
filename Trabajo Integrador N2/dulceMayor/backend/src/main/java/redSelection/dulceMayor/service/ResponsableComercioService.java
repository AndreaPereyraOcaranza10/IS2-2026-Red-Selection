package redSelection.dulceMayor.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 PATRÓN: Template Method (capa Service). El correo y el teléfono se guardan como contactos principales:
 el correo es el que usa NotificacionFacade para avisar "es tu turno" al comercio.
 */
@Service
public class ResponsableComercioService extends BaseService<ResponsableComercio, ResponsableCrearDTO, ResponsableRespuestaDTO> {

    private static final int MAXIMO_RESULTADOS = 20;

    private final ResponsableComercioRepository responsableRepository;

    public ResponsableComercioService(ResponsableComercioRepository responsableRepository) {
        super(responsableRepository);
        this.responsableRepository = responsableRepository;
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

    @Override
    protected void aplicarCambios(ResponsableComercio responsable, ResponsableCrearDTO dto) {
        responsable.setNombre(dto.nombre().trim());
        responsable.setApellido(dto.apellido().trim());
        responsable.setTipoDocumento(dto.tipoDocumento());
        responsable.setNumeroDocumento(documento(dto));
        principal(responsable, ContactoCorreoElectronico.class).ifPresentOrElse(
                c -> c.setEmail(correo(dto)),
                () -> responsable.getContactos().add(nuevoCorreo(dto)));
        principal(responsable, ContactoTelefonico.class).ifPresentOrElse(
                c -> {
                    c.setTelefono(dto.telefono());
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
        validarFormatoDocumento(dto);
        if (responsableRepository.existsByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(
                dto.tipoDocumento(), documento(dto))) {
            throw new ReglaNegocioException(
                    "Ya existe un responsable con ese documento. Elegilo de la lista en lugar de cargarlo de nuevo.");
        }
    }

    @Override
    protected void validarActualizacion(ResponsableComercio actual, ResponsableCrearDTO dto) {
        validarFormatoDocumento(dto);
        boolean cambioDocumento = actual.getTipoDocumento() != dto.tipoDocumento()
                || !actual.getNumeroDocumento().equals(documento(dto));
        if (cambioDocumento && responsableRepository.existsByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(
                dto.tipoDocumento(), documento(dto))) {
            throw new ReglaNegocioException("Ya existe otro responsable con ese documento.");
        }
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
        return responsableRepository.save(aEntidad(dto));
    }

    // ---------- Auxiliares ----------

    private void validarFormatoDocumento(ResponsableCrearDTO dto) {
        if (dto.tipoDocumento() == TipoDocumento.DNI && !dto.numeroDocumento().trim().matches("\\d{7,8}")) {
            throw new ReglaNegocioException("El DNI debe tener 7 u 8 números.");
        }
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
                .telefono(dto.telefono()).tipoTelefono(tipoTelefono(dto)).build();
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