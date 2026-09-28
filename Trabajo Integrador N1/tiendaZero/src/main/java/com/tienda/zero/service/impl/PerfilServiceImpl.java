package com.tienda.zero.service.impl;

import com.tienda.zero.dto.PerfilDTO;
import com.tienda.zero.enums.TipoContacto;
import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoTelefono;
import com.tienda.zero.model.*;
import com.tienda.zero.repository.*;
import com.tienda.zero.service.PerfilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerfilServiceImpl implements PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;
    private final DireccionRepository direccionRepository;
    private final LocalidadRepository localidadRepository;
    private final DepartamentoRepository departamentoRepository;
    private final ProvinciaRepository provinciaRepository;
    private final PaisRepository paisRepository;
    private final NacionalidadRepository nacionalidadRepository;
    private final ContactoTelefonicoRepository contactoTelefonicoRepository;
    private final ContactoCorreoElectronicoRepository contactoCorreoElectronicoRepository;

    @Override
    @Transactional(readOnly = true)
    public PerfilDTO obtenerPerfilUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));

        PerfilDTO dto = PerfilDTO.builder()
                .email(usuario.getEmail())
                .tipoDocumento(TipoDocumento.DNI)
                .tipoTelefono(TipoTelefono.CELULAR)
                .provincia("Mendoza")
                .departamento("Capital")
                .localidad("Ciudad de Mendoza")
                .codigoPostal("5500")
                .build();

        Persona persona = usuario.getPersona();
        if (persona != null) {
            dto.setNombre(persona.getNombre());
            dto.setApellido(persona.getApellido());
            dto.setSexo(persona.getSexo());
            if (persona.getFechaNacimiento() != null) {
                dto.setFechaNacimiento(persona.getFechaNacimiento().toString());
            }
            dto.setTipoDocumento(persona.getTipoDocumento());
            dto.setNumeroDocumento(persona.getNumeroDocumento());

            // Obtener teléfono de los contactos
            if (persona.getContactos() != null) {
                for (Contacto c : persona.getContactos()) {
                    if (c instanceof ContactoTelefonico ct && !ct.isEliminado()) {
                        dto.setTelefono(ct.getTelefono());
                        dto.setTipoTelefono(ct.getTipoTelefono());
                        break;
                    }
                }
            }

            // Obtener datos de la dirección principal
            if (persona.getDirecciones() != null && !persona.getDirecciones().isEmpty()) {
                for (Direccion d : persona.getDirecciones()) {
                    if (!d.isEliminado()) {
                        dto.setCalle(d.getCalle());
                        dto.setNumeroCalle(d.getNumeracion());
                        dto.setManzanaPiso(d.getManzanaPiso());
                        dto.setCasaDepartamento(d.getCasaDepartamento());
                        dto.setReferencia(d.getReferencia());

                        if (d.getLocalidad() != null) {
                            dto.setLocalidad(d.getLocalidad().getNombre());
                            dto.setCodigoPostal(d.getLocalidad().getCodigoPostal());

                            if (d.getLocalidad().getDepartamento() != null) {
                                dto.setDepartamento(d.getLocalidad().getDepartamento().getNombre());

                                if (d.getLocalidad().getDepartamento().getProvincia() != null) {
                                    dto.setProvincia(d.getLocalidad().getDepartamento().getProvincia().getNombre());
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }

        return dto;
    }

    @Override
    @Transactional
    public void guardarPerfilUsuario(String email, PerfilDTO dto) {
        // Validaciones básicas de campos obligatorios
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (dto.getApellido() == null || dto.getApellido().isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        if (dto.getFechaNacimiento() == null || dto.getFechaNacimiento().isBlank()) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }
        if (dto.getNumeroDocumento() == null || dto.getNumeroDocumento().isBlank()) {
            throw new IllegalArgumentException("El número de documento es obligatorio.");
        }
        if (dto.getCalle() == null || dto.getCalle().isBlank()) {
            throw new IllegalArgumentException("La calle es obligatoria.");
        }
        if (dto.getNumeroCalle() == null || dto.getNumeroCalle().isBlank()) {
            throw new IllegalArgumentException("El número de calle es obligatorio.");
        }
        if (dto.getTelefono() == null || dto.getTelefono().isBlank()) {
            throw new IllegalArgumentException("El número de teléfono es obligatorio.");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));

        Date fechaNac = Date.valueOf(dto.getFechaNacimiento());
        TipoDocumento tipoDoc = dto.getTipoDocumento() != null ? dto.getTipoDocumento() : TipoDocumento.DNI;
        TipoTelefono tipoTel = dto.getTipoTelefono() != null ? dto.getTipoTelefono() : TipoTelefono.CELULAR;

        // 1. Asegurar País (Argentina)
        Pais pais = paisRepository.findByNombre("Argentina")
                .orElseGet(() -> paisRepository.save(Pais.builder().nombre("Argentina").eliminado(false).build()));

        // 2. Asegurar Provincia
        String nombreProvincia = (dto.getProvincia() != null && !dto.getProvincia().isBlank()) ? dto.getProvincia().trim() : "Mendoza";
        Provincia provincia = provinciaRepository.findByNombre(nombreProvincia)
                .orElseGet(() -> provinciaRepository.save(Provincia.builder().nombre(nombreProvincia).pais(pais).eliminado(false).build()));

        // 3. Asegurar Departamento
        String nombreDepto = (dto.getDepartamento() != null && !dto.getDepartamento().isBlank()) ? dto.getDepartamento().trim() : "Capital";
        Departamento departamento = departamentoRepository.findByNombre(nombreDepto)
                .orElseGet(() -> departamentoRepository.save(Departamento.builder().nombre(nombreDepto).provincia(provincia).eliminado(false).build()));

        // 4. Asegurar Localidad
        String nombreLoc = (dto.getLocalidad() != null && !dto.getLocalidad().isBlank()) ? dto.getLocalidad().trim() : "Ciudad de Mendoza";
        String cp = (dto.getCodigoPostal() != null && !dto.getCodigoPostal().isBlank()) ? dto.getCodigoPostal().trim() : "5500";
        Localidad localidad = localidadRepository.findByNombre(nombreLoc)
                .orElseGet(() -> localidadRepository.save(Localidad.builder().nombre(nombreLoc).codigoPostal(cp).departamento(departamento).eliminado(false).build()));

        // 5. Crear o actualizar Dirección
        Direccion direccion;
        Persona persona = usuario.getPersona();

        if (persona != null && !persona.getDirecciones().isEmpty()) {
            direccion = persona.getDirecciones().get(0);
            direccion.setCalle(dto.getCalle().trim());
            direccion.setNumeracion(dto.getNumeroCalle().trim());
            direccion.setManzanaPiso(dto.getManzanaPiso());
            direccion.setCasaDepartamento(dto.getCasaDepartamento());
            direccion.setReferencia(dto.getReferencia());
            direccion.setLocalidad(localidad);
        } else {
            direccion = Direccion.builder()
                    .calle(dto.getCalle().trim())
                    .numeracion(dto.getNumeroCalle().trim())
                    .manzanaPiso(dto.getManzanaPiso())
                    .casaDepartamento(dto.getCasaDepartamento())
                    .referencia(dto.getReferencia())
                    .localidad(localidad)
                    .eliminado(false)
                    .build();
        }
        direccion = direccionRepository.save(direccion);

        // 6. Crear o actualizar Contacto Telefónico
        ContactoTelefonico contactoTel = null;
        if (persona != null && persona.getContactos() != null) {
            for (Contacto c : persona.getContactos()) {
                if (c instanceof ContactoTelefonico ct && !ct.isEliminado()) {
                    contactoTel = ct;
                    contactoTel.setTelefono(dto.getTelefono().trim());
                    contactoTel.setTipoTelefono(tipoTel);
                    break;
                }
            }
        }
        if (contactoTel == null) {
            contactoTel = ContactoTelefonico.builder()
                    .telefono(dto.getTelefono().trim())
                    .tipoTelefono(tipoTel)
                    .tipoContacto(TipoContacto.PERSONAL)
                    .observacion("Teléfono de contacto personal")
                    .eliminado(false)
                    .build();
        }
        contactoTel = contactoTelefonicoRepository.save(contactoTel);

        // 7. Crear o actualizar Contacto de Correo Electrónico
        ContactoCorreoElectronico contactoEmail = null;
        if (persona != null && persona.getContactos() != null) {
            for (Contacto c : persona.getContactos()) {
                if (c instanceof ContactoCorreoElectronico ce && !ce.isEliminado()) {
                    contactoEmail = ce;
                    break;
                }
            }
        }
        if (contactoEmail == null) {
            contactoEmail = ContactoCorreoElectronico.builder()
                    .email(usuario.getEmail())
                    .tipoContacto(TipoContacto.PERSONAL)
                    .observacion("Correo personal")
                    .eliminado(false)
                    .build();
            contactoEmail = contactoCorreoElectronicoRepository.save(contactoEmail);
        }

        // 8. Asegurar Nacionalidad para Cliente
        Nacionalidad nacionalidad = nacionalidadRepository.findByNombreIgnoreCase("Argentina")
                .orElseGet(() -> nacionalidadRepository.save(Nacionalidad.builder().nombre("Argentina").eliminado(false).build()));

        // 9. Crear o actualizar Cliente / Persona
        if (persona == null) {
            Cliente nuevoCliente = Cliente.builder()
                    .nombre(dto.getNombre().trim())
                    .apellido(dto.getApellido().trim())
                    .sexo(dto.getSexo())
                    .fechaNacimiento(fechaNac)
                    .tipoDocumento(tipoDoc)
                    .numeroDocumento(dto.getNumeroDocumento().trim())
                    .direccionEstadia(dto.getCalle().trim() + " " + dto.getNumeroCalle().trim())
                    .nacionalidad(nacionalidad)
                    .eliminado(false)
                    .build();

            nuevoCliente.getDirecciones().add(direccion);
            nuevoCliente.getContactos().add(contactoTel);
            nuevoCliente.getContactos().add(contactoEmail);

            nuevoCliente = clienteRepository.save(nuevoCliente);
            usuario.setPersona(nuevoCliente);
            usuarioRepository.save(usuario);
        } else {
            persona.setNombre(dto.getNombre().trim());
            persona.setApellido(dto.getApellido().trim());
            persona.setSexo(dto.getSexo());
            persona.setFechaNacimiento(fechaNac);
            persona.setTipoDocumento(tipoDoc);
            persona.setNumeroDocumento(dto.getNumeroDocumento().trim());

            if (!persona.getDirecciones().contains(direccion)) {
                persona.getDirecciones().add(direccion);
            }
            if (!persona.getContactos().contains(contactoTel)) {
                persona.getContactos().add(contactoTel);
            }
            if (!persona.getContactos().contains(contactoEmail)) {
                persona.getContactos().add(contactoEmail);
            }

            personaRepository.save(persona);
        }

        log.info("Perfil guardado exitosamente para el usuario: {}", email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tienePerfilCompleto(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailIgnoreCase(email);
        if (usuarioOpt.isEmpty()) return false;
        Usuario usuario = usuarioOpt.get();
        Persona persona = usuario.getPersona();
        return persona != null
                && persona.getNombre() != null && !persona.getNombre().isBlank()
                && persona.getApellido() != null && !persona.getApellido().isBlank()
                && !persona.getDirecciones().isEmpty()
                && !persona.getContactos().isEmpty();
    }
}
