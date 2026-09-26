package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoUsuario;
import com.tienda.zero.model.*;
import com.tienda.zero.repository.PersonaRepository;
import com.tienda.zero.service.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository personaRepository;
    private final ImagenService imagenService;
    private final ContactoService contactoService;
    private final DireccionService direccionService;
    private final UsuarioService usuarioService;

    public PersonaServiceImpl(PersonaRepository personaRepository, ImagenService imagenService,
                              ContactoService contactoService, DireccionService direccionService, UsuarioService usuarioService) {
        this.personaRepository = personaRepository;
        this.imagenService = imagenService;
        this.contactoService = contactoService;
        this.direccionService = direccionService;
        this.usuarioService = usuarioService;
    }

    @Override
    public void validar(String nombre, String apellido, Date fechaNacimiento,
                        TipoDocumento tipoDocumento, String numeroDocumento) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        if (tipoDocumento == null) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio");
        }
        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            throw new IllegalArgumentException("El número de documento es obligatorio");
        }

        //ya no validamos si existe una persona con el mismo documento porque ahora lo haceoms en los roles

    }


    @Override
    public void validarParaModificar(String id, String nombre, String apellido, Date fechaNacimiento,
                                     TipoDocumento tipoDocumento, String numeroDocumento) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        if (tipoDocumento == null) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio");
        }
        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            throw new IllegalArgumentException("El número de documento es obligatorio");
        }
        //ya no validamos si existe una persona con el mismo documento porque ahora lo haceoms en los roles
    }

    @Override
    public Persona buscarPersona(String id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + id));
    }


    //ahora eliminar persona elimina usuario
    @Override
    public void eliminarPersona(String id) {
        Persona persona = buscarPersona(id);
        persona.setEliminado(true);
        if (persona.getUsuario() != null) {
            usuarioService.eliminarUsuario(persona.getUsuario().getId());
        }
        personaRepository.save(persona);
    }

    @Override
    public Persona asociarImagenPersona(String id, String idImagen) {
        Persona persona = buscarPersona(id);
        Imagen imagen = imagenService.buscarImagen(idImagen);
        persona.setImagen(imagen);
        return personaRepository.save(persona);
    }

    @Override
    public Persona asociarContactoPersona(String id, String idContacto) {
        Persona persona = buscarPersona(id);
        Contacto contacto = contactoService.buscarContacto(idContacto);

        if (contacto.isEliminado()) {
            throw new IllegalArgumentException("No se puede asociar un contacto eliminado");
        }
        persona.getContactos().add(contacto);
        return personaRepository.save(persona);
    }

    @Override
    public Persona asociarDireccionPersona(String id, String idDireccion) {
        Persona persona = buscarPersona(id);
        Direccion direccion = direccionService.buscarDireccion(idDireccion);

        if (direccion.isEliminado()) {
            throw new IllegalArgumentException("No se puede asociar una dirección eliminada");
        }
        persona.getDirecciones().add(direccion);
        return personaRepository.save(persona);
    }

    @Override
    @Transactional
    public Persona asociarUsuarioPersona(String id, String idUsuario) {
        Persona persona = buscarPersona(id);
        Usuario usuario = usuarioService.buscarUsuario(idUsuario);

        if (persona.isEliminado()) {
            throw new IllegalArgumentException("No se puede asociar un usuario a una persona eliminada");
        }
        if (usuario.isEliminado()) {
            throw new IllegalArgumentException("No se puede asociar un usuario eliminado");
        }
        if (persona.getUsuario() != null) {
            throw new IllegalArgumentException("La persona ya tiene un usuario asociado");
        }

        Optional<Persona> duenioActual = personaRepository.findByUsuarioId(idUsuario);
        if (duenioActual.isPresent()) {
            throw new IllegalArgumentException("El usuario ya está asociado a otra persona");
        }

        validarRolSegunTipoPersona(persona, usuario.getRol());

        persona.setUsuario(usuario);
        return personaRepository.save(persona);
    }

    private void validarRolSegunTipoPersona(Persona persona, TipoUsuario rol) {
        if (persona instanceof Cliente && rol != TipoUsuario.CLIENTE) {
            throw new IllegalArgumentException("Un cliente solo puede tener un usuario con rol CLIENTE");
        }
        if (persona instanceof Empleado empleado
                && !rol.name().equals(empleado.getTipoEmpleado().name())) {
            throw new IllegalArgumentException("El rol del usuario debe coincidir con el tipo de empleado");
        }
    }
}