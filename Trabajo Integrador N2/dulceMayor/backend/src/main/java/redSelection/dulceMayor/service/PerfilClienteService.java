package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.PerfilClienteDTO;
import redSelection.dulceMayor.dto.PerfilClienteRespuestaDTO;
import redSelection.dulceMayor.dto.UbicacionResueltaDTO;
import redSelection.dulceMayor.enums.TipoContacto;
import redSelection.dulceMayor.enums.TipoDocumento;
import redSelection.dulceMayor.enums.TipoTelefono;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.ContactoCorreoElectronico;
import redSelection.dulceMayor.model.ContactoTelefonico;
import redSelection.dulceMayor.model.Direccion;
import redSelection.dulceMayor.model.ResponsableComercio;
import redSelection.dulceMayor.model.Usuario;
import redSelection.dulceMayor.validacion.ValidadorPersona;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * Perfil del cliente minorista en el portal institucional (ver y completar sus datos).
 * Antes esta lógica estaba dentro del controller; ahora vive en la capa Service, con las reglas de
 * ValidadorPersona, tanto la primera vez que completa el perfil (alta) como cuando lo modifica.
 * El correo NO se modifica: es su usuario de acceso, y se muestra solo como lectura.
 */
@Service
public class PerfilClienteService {

    private final CuentaClienteService cuentas;
    private final ResponsableComercioService responsables;
    private final UbicacionService ubicaciones;

    public PerfilClienteService(CuentaClienteService cuentas,
                                ResponsableComercioService responsables,
                                UbicacionService ubicaciones) {
        this.cuentas = cuentas;
        this.responsables = responsables;
        this.ubicaciones = ubicaciones;
    }

    @Transactional(readOnly = true)
    public PerfilClienteRespuestaDTO obtener(String nombreUsuario) {
        Usuario usuario = cuentas.obtenerUsuarioActivo(nombreUsuario);
        return responsables.buscarPorNombreUsuario(usuario.getNombreUsuario())
                .map(this::aRespuesta)
                .orElseGet(() -> PerfilClienteRespuestaDTO.vacio(usuario.getNombreUsuario()));
    }

    @Transactional
    public PerfilClienteRespuestaDTO actualizar(String nombreUsuario, PerfilClienteDTO dto) {
        Usuario usuario = cuentas.obtenerUsuarioActivo(nombreUsuario);
        ResponsableComercio persona = responsables.buscarPorNombreUsuario(usuario.getNombreUsuario())
                .orElseGet(() -> ResponsableComercio.builder().usuario(usuario).contactos(new ArrayList<>()).build());
        boolean esAlta = persona.getId() == null;

        // Reglas de datos personales (mismas del alta y de la modificación)
        String nombre = ValidadorPersona.nombre("nombre", dto.nombre());
        String apellido = ValidadorPersona.nombre("apellido", dto.apellido());
        TipoDocumento tipoDocumento = dto.tipoDocumento() == null ? TipoDocumento.DNI : dto.tipoDocumento();
        String documento = ValidadorPersona.documento(tipoDocumento, dto.numeroDocumento());
        String telefono = ValidadorPersona.telefono(dto.telefono());
        responsables.exigirDocumentoDisponible(tipoDocumento, documento, esAlta ? null : persona);

        UbicacionResueltaDTO ubicacion;
        try {
            ubicacion = ubicaciones.resolver(dto.provinciaId(), dto.departamentoId(), dto.localidadId());
        } catch (RuntimeException e) {
            throw new ReglaNegocioException("Seleccioná una ubicación válida.");
        }
        LocalDate nacimiento;
        try {
            nacimiento = LocalDate.parse(dto.fechaNacimiento());
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("La fecha de nacimiento no es válida.");
        }
        if (nacimiento.isAfter(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha de nacimiento no puede ser futura.");
        }

        persona.setUsuario(usuario);
        persona.setNombre(nombre);
        persona.setApellido(apellido);
        persona.setTipoDocumento(tipoDocumento);
        persona.setNumeroDocumento(documento);
        persona.setSexo(dto.sexo());
        persona.setFechaNacimiento(nacimiento);

        Direccion direccion = persona.getDireccion();
        if (direccion == null) {
            direccion = Direccion.builder().build();
            persona.setDireccion(direccion);
        }
        direccion.setCalle(dto.calle().trim());
        direccion.setNumeracion(dto.numeroCalle().trim());
        direccion.setBarrio(null);
        direccion.setReferencia(limpiar(dto.referencia()));
        direccion.setManzanaPiso(limpiar(dto.manzanaPiso()));
        direccion.setCasaDepartamento(limpiar(dto.casaDepartamento()));
        direccion.setCodigoPostal(limpiar(dto.codigoPostal()));
        direccion.setProvinciaId(ubicacion.provincia().id());
        direccion.setProvinciaNombre(ubicacion.provincia().nombre());
        direccion.setDepartamentoId(ubicacion.departamento().id());
        direccion.setDepartamentoNombre(ubicacion.departamento().nombre());
        direccion.setLocalidadId(ubicacion.localidad().id());
        direccion.setLocalidadNombre(ubicacion.localidad().nombre());

        ContactoTelefonico contactoTelefono = persona.getContactos().stream()
                .filter(c -> !c.isEliminado() && c instanceof ContactoTelefonico)
                .map(c -> (ContactoTelefonico) c).findFirst().orElse(null);
        if (contactoTelefono == null) {
            contactoTelefono = ContactoTelefonico.builder().tipoContacto(TipoContacto.PERSONAL).principal(true).build();
            persona.getContactos().add(contactoTelefono);
        }
        contactoTelefono.setTelefono(telefono);
        contactoTelefono.setTipoTelefono(dto.tipoTelefono() == null ? TipoTelefono.CELULAR : dto.tipoTelefono());

        // El correo no se modifica: si la persona todavía no tiene uno cargado se le asigna el de su usuario, una sola vez.
        boolean tieneCorreo = persona.getContactos().stream()
                .anyMatch(c -> !c.isEliminado() && c instanceof ContactoCorreoElectronico);
        if (!tieneCorreo) {
            persona.getContactos().add(ContactoCorreoElectronico.builder().tipoContacto(TipoContacto.PERSONAL)
                    .observacion("Correo de acceso").principal(true).email(usuario.getNombreUsuario()).build());
        }
        return aRespuesta(responsables.guardar(persona));
    }

    private PerfilClienteRespuestaDTO aRespuesta(ResponsableComercio p) {
        Direccion d = p.getDireccion();
        ContactoTelefonico telefono = p.getContactos().stream()
                .filter(c -> !c.isEliminado() && c instanceof ContactoTelefonico)
                .map(c -> (ContactoTelefonico) c).findFirst().orElse(null);
        return new PerfilClienteRespuestaDTO(
                p.getNombre(), p.getApellido(), p.getSexo(),
                p.getFechaNacimiento() == null ? "" : p.getFechaNacimiento().toString(),
                p.getTipoDocumento(), p.getNumeroDocumento(), p.getUsuario().getNombreUsuario(),
                telefono == null ? "" : telefono.getTelefono(),
                telefono == null ? TipoTelefono.CELULAR : telefono.getTipoTelefono(),
                d == null ? "" : d.getProvinciaNombre(), d == null ? "" : d.getDepartamentoNombre(),
                d == null ? "" : d.getLocalidadNombre(), d == null ? "" : texto(d.getCodigoPostal()),
                d == null ? "" : d.getCalle(), d == null ? "" : d.getNumeracion(),
                d == null ? "" : texto(d.getManzanaPiso()), d == null ? "" : texto(d.getCasaDepartamento()),
                d == null ? "" : texto(d.getReferencia()), d == null ? "" : d.getProvinciaId(),
                d == null ? "" : d.getDepartamentoId(), d == null ? "" : d.getLocalidadId());
    }

    private static String limpiar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    private static String texto(String valor) { return valor == null ? "" : valor; }
}
