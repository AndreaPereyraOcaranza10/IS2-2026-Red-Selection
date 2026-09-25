package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.model.Contacto;
import com.tienda.zero.model.Direccion;
import com.tienda.zero.model.Imagen;
import com.tienda.zero.model.Persona;
import com.tienda.zero.repository.PersonaRepository;
import com.tienda.zero.service.ContactoService;
import com.tienda.zero.service.DireccionService;
import com.tienda.zero.service.ImagenService;
import com.tienda.zero.service.PersonaService;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Service
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository personaRepository;
    private final ImagenService imagenService;
    private final ContactoService contactoService;
    private final DireccionService direccionService;

    public PersonaServiceImpl(PersonaRepository personaRepository, ImagenService imagenService,
                              ContactoService contactoService, DireccionService direccionService) {
        this.personaRepository = personaRepository;
        this.imagenService = imagenService;
        this.contactoService = contactoService;
        this.direccionService = direccionService;
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

        personaRepository.findByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento).ifPresent(p -> {
            throw new IllegalArgumentException("Ya existe una persona con ese documento");
        });
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
        personaRepository.findByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento)
                .filter(p -> !p.getId().equals(id))
                .ifPresent(p -> {
                    throw new IllegalArgumentException("Ya existe otra persona con ese documento");
                });
    }

    @Override
    public Persona buscarPersona(String id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + id));
    }


    @Override
    public void eliminarPersona(String id) {
        Persona persona = buscarPersona(id);
        persona.setEliminado(true);
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
}