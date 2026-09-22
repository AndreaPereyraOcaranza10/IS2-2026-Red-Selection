package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.model.Imagen;
import com.tienda.zero.model.Persona;
import com.tienda.zero.repository.PersonaRepository;
import com.tienda.zero.service.ImagenService;
import com.tienda.zero.service.PersonaService;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository personaRepository;
    private final ImagenService imagenService;

    public PersonaServiceImpl(PersonaRepository personaRepository, ImagenService imagenService) {
        this.personaRepository = personaRepository;
        this.imagenService = imagenService;
    }

    @Override
    public Persona crearPersona(String nombre, String apellido, Date fechaNacimiento,
                                TipoDocumento tipoDocumento, String numeroDocumento) {
        validar(nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento);

        Persona persona = Persona.builder()
                .nombre(nombre)
                .apellido(apellido)
                .fechaNacimiento(fechaNacimiento)
                .tipoDocumento(tipoDocumento)
                .numeroDocumento(numeroDocumento)
                .eliminado(false)
                .build();

        return personaRepository.save(persona);
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
    public Persona buscarPersona(String id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + id));
    }

    @Override
    public Persona modificarPersona(String id, String nombre, String apellido, Date fechaNacimiento,
                                    TipoDocumento tipoDocumento, String numeroDocumento) {
        Persona persona = buscarPersona(id);

        personaRepository.findByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento)
                .filter(p -> !p.getId().equals(id))
                .ifPresent(p -> {
                    throw new IllegalArgumentException("Ya existe otra persona con ese documento");
                });

        persona.setNombre(nombre);
        persona.setApellido(apellido);
        persona.setFechaNacimiento(fechaNacimiento);
        persona.setTipoDocumento(tipoDocumento);
        persona.setNumeroDocumento(numeroDocumento);

        return personaRepository.save(persona);
    }

    @Override
    public void eliminarPersona(String id) {
        Persona persona = buscarPersona(id);
        persona.setEliminado(true);
        personaRepository.save(persona);
    }

    @Override
    public List<Persona> listarPersona() {
        return personaRepository.findAll();
    }

    @Override
    public List<Persona> listarPersonaActivo() {
        return personaRepository.findByEliminadoFalse();
    }

    @Override
    public Persona asociarImagenPersona(String id, String idImagen) {
        Persona persona = buscarPersona(id);
        Imagen imagen = imagenService.buscarImagen(idImagen);
        persona.setImagen(imagen);
        return personaRepository.save(persona);
    }
}