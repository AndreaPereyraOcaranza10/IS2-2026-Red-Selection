package redSelection.dulceMayor.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import redSelection.dulceMayor.dto.UbicacionResueltaDTO;
import redSelection.dulceMayor.enums.Sexo;
import redSelection.dulceMayor.enums.TipoContacto;
import redSelection.dulceMayor.enums.TipoDocumento;
import redSelection.dulceMayor.enums.TipoTelefono;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.repository.ResponsableComercioRepository;
import redSelection.dulceMayor.repository.UsuarioRepository;
import redSelection.dulceMayor.service.UbicacionService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/profile")
public class PortalProfileController {
    private final UsuarioRepository usuarios;
    private final ResponsableComercioRepository responsables;
    private final UbicacionService ubicaciones;

    public PortalProfileController(UsuarioRepository usuarios, ResponsableComercioRepository responsables,
                                   UbicacionService ubicaciones) {
        this.usuarios = usuarios;
        this.responsables = responsables;
        this.ubicaciones = ubicaciones;
    }

    @GetMapping
    @Transactional
    public ProfileResponse profile(Authentication authentication) {
        Usuario usuario = usuario(authentication);
        return responsables.findByEliminadoFalse().stream()
                .filter(r -> r.getUsuario() != null
                        && r.getUsuario().getNombreUsuario().equalsIgnoreCase(usuario.getNombreUsuario()))
                .findFirst().map(this::toResponse)
                .orElseGet(() -> ProfileResponse.empty(usuario.getNombreUsuario()));
    }

    @PutMapping
    @Transactional
    public ProfileResponse update(Authentication authentication, @Valid @RequestBody ProfileRequest request) {
        Usuario usuario = usuario(authentication);
        ResponsableComercio persona = responsables.findByEliminadoFalse().stream()
                .filter(r -> r.getUsuario() != null
                        && r.getUsuario().getNombreUsuario().equalsIgnoreCase(usuario.getNombreUsuario()))
                .findFirst().orElseGet(() -> ResponsableComercio.builder()
                        .usuario(usuario).contactos(new ArrayList<>()).build());

        UbicacionResueltaDTO location;
        try {
            location = ubicaciones.resolver(request.provinciaId(), request.departamentoId(), request.localidadId());
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seleccioná una ubicación válida.");
        }
        LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(request.fechaNacimiento());
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de nacimiento no es válida.");
        }

        persona.setUsuario(usuario);
        persona.setNombre(request.nombre().trim());
        persona.setApellido(request.apellido().trim());
        persona.setTipoDocumento(request.tipoDocumento() == null ? TipoDocumento.DNI : request.tipoDocumento());
        persona.setNumeroDocumento(request.numeroDocumento().trim());
        persona.setSexo(request.sexo());
        persona.setFechaNacimiento(birthDate);

        Direccion address = persona.getDireccion();
        if (address == null) {
            address = Direccion.builder().build();
            persona.setDireccion(address);
        }
        address.setCalle(request.calle().trim());
        address.setNumeracion(request.numeroCalle().trim());
        address.setBarrio(null);
        address.setReferencia(clean(request.referencia()));
        address.setManzanaPiso(clean(request.manzanaPiso()));
        address.setCasaDepartamento(clean(request.casaDepartamento()));
        address.setCodigoPostal(clean(request.codigoPostal()));
        address.setProvinciaId(location.provincia().id());
        address.setProvinciaNombre(location.provincia().nombre());
        address.setDepartamentoId(location.departamento().id());
        address.setDepartamentoNombre(location.departamento().nombre());
        address.setLocalidadId(location.localidad().id());
        address.setLocalidadNombre(location.localidad().nombre());

        ContactoTelefonico phone = persona.getContactos().stream()
                .filter(c -> !c.isEliminado() && c instanceof ContactoTelefonico)
                .map(c -> (ContactoTelefonico) c).findFirst().orElse(null);
        if (phone == null) {
            phone = ContactoTelefonico.builder().tipoContacto(TipoContacto.PERSONAL)
                    .principal(true).build();
            persona.getContactos().add(phone);
        }
        phone.setTelefono(request.telefono().trim());
        phone.setTipoTelefono(request.tipoTelefono() == null ? TipoTelefono.CELULAR : request.tipoTelefono());

        ContactoCorreoElectronico email = persona.getContactos().stream()
                .filter(c -> !c.isEliminado() && c instanceof ContactoCorreoElectronico)
                .map(c -> (ContactoCorreoElectronico) c).findFirst().orElse(null);
        if (email == null) {
            persona.getContactos().add(ContactoCorreoElectronico.builder().tipoContacto(TipoContacto.PERSONAL)
                    .observacion("Correo de acceso").principal(true).email(usuario.getNombreUsuario()).build());
        } else {
            email.setEmail(usuario.getNombreUsuario());
        }

        return toResponse(responsables.save(persona));
    }

    private Usuario usuario(Authentication authentication) {
        if (authentication == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return usuarios.findByNombreUsuarioIgnoreCase(authentication.getName())
                .filter(u -> u.isActivo() && !u.isEliminado())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private ProfileResponse toResponse(ResponsableComercio p) {
        Direccion d = p.getDireccion();
        String phone = p.getContactos().stream().filter(c -> !c.isEliminado() && c instanceof ContactoTelefonico)
                .map(c -> (ContactoTelefonico) c).map(ContactoTelefonico::getTelefono).findFirst().orElse("");
        TipoTelefono phoneType = p.getContactos().stream().filter(c -> !c.isEliminado() && c instanceof ContactoTelefonico)
                .map(c -> (ContactoTelefonico) c).map(ContactoTelefonico::getTipoTelefono).findFirst().orElse(TipoTelefono.CELULAR);
        return new ProfileResponse(p.getNombre(), p.getApellido(), p.getSexo(),
                p.getFechaNacimiento() == null ? "" : p.getFechaNacimiento().toString(),
                p.getTipoDocumento(), p.getNumeroDocumento(), p.getUsuario().getNombreUsuario(), phone, phoneType,
                d == null ? "" : d.getProvinciaNombre(), d == null ? "" : d.getDepartamentoNombre(),
                d == null ? "" : d.getLocalidadNombre(), d == null ? "" : value(d.getCodigoPostal()),
                d == null ? "" : d.getCalle(), d == null ? "" : d.getNumeracion(),
                d == null ? "" : value(d.getManzanaPiso()), d == null ? "" : value(d.getCasaDepartamento()),
                d == null ? "" : value(d.getReferencia()), d == null ? "" : d.getProvinciaId(),
                d == null ? "" : d.getDepartamentoId(), d == null ? "" : d.getLocalidadId());
    }

    private String value(String value) { return value == null ? "" : value; }

    public record ProfileRequest(@NotBlank String nombre, @NotBlank String apellido, Sexo sexo,
                                 @NotBlank String fechaNacimiento, TipoDocumento tipoDocumento,
                                 @NotBlank String numeroDocumento, @NotBlank String telefono,
                                 TipoTelefono tipoTelefono, @NotBlank String provinciaId,
                                 @NotBlank String departamentoId, @NotBlank String localidadId,
                                 @NotBlank String calle, @NotBlank String numeroCalle,
                                 String codigoPostal, String manzanaPiso, String casaDepartamento, String referencia) {}

    public record ProfileResponse(String nombre, String apellido, Sexo sexo, String fechaNacimiento,
                                  TipoDocumento tipoDocumento, String numeroDocumento, String email,
                                  String telefono, TipoTelefono tipoTelefono, String provincia, String departamento,
                                  String localidad, String codigoPostal, String calle, String numeroCalle,
                                  String manzanaPiso, String casaDepartamento, String referencia,
                                  String provinciaId, String departamentoId, String localidadId) {
        static ProfileResponse empty(String email) {
            return new ProfileResponse("", "", null, "", TipoDocumento.DNI, "", email, "",
                    TipoTelefono.CELULAR, "", "", "", "", "", "", "", "", "", "", "", "");
        }
    }
}