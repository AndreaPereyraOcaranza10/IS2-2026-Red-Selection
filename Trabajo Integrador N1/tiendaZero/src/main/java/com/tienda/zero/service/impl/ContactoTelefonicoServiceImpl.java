package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoContacto;
import com.tienda.zero.enums.TipoTelefono;
import com.tienda.zero.model.ContactoTelefonico;
import com.tienda.zero.repository.ContactoTelefonicoRepository;
import com.tienda.zero.service.ContactoService;
import com.tienda.zero.service.ContactoTelefonicoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ContactoTelefonicoServiceImpl implements ContactoTelefonicoService {

    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^[+0-9\\s()-]{6,20}$");

    private final ContactoTelefonicoRepository contactoTelefonicoRepository;
    private final ContactoService contactoService;

    @Override
    @Transactional
    public ContactoTelefonico crearContactoTelefonico(String telefono, TipoTelefono tipoTelefono,
                                                      TipoContacto tipoContacto, String observacion) {
        validar(telefono, tipoTelefono, tipoContacto, observacion);

        ContactoTelefonico contacto = ContactoTelefonico.builder()
                .telefono(telefono.trim())
                .tipoTelefono(tipoTelefono)
                .tipoContacto(tipoContacto)
                .observacion(observacion)
                .build();

        return contactoTelefonicoRepository.save(contacto);
    }

    @Override
    public void validar(String telefono, TipoTelefono tipoTelefono,
                        TipoContacto tipoContacto, String observacion) {
        contactoService.validar(tipoContacto, observacion);

        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono es obligatorio");
        }
        if (!TELEFONO_PATTERN.matcher(telefono.trim()).matches()) {
            throw new IllegalArgumentException("El formato del teléfono no es válido");
        }
        if (tipoTelefono == null) {
            throw new IllegalArgumentException("El tipo de teléfono es obligatorio");
        }
    }

    @Override
    @Transactional
    public void modificarContactoTelefonico(String id, String telefono, TipoTelefono tipoTelefono,
                                            TipoContacto tipoContacto, String observacion) {
        ContactoTelefonico contacto = contactoTelefonicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el teléfono con id: " + id));

        if (contacto.isEliminado()) {
            throw new IllegalArgumentException("No se puede modificar un contacto eliminado");
        }

        validar(telefono, tipoTelefono, tipoContacto, observacion);

        contacto.setTelefono(telefono.trim());
        contacto.setTipoTelefono(tipoTelefono);
        contacto.setTipoContacto(tipoContacto);
        contacto.setObservacion(observacion);

        contactoTelefonicoRepository.save(contacto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactoTelefonico> listarContactoTelefonico() {
        return contactoTelefonicoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactoTelefonico> listarContactoTelefonicoActivo() {
        return contactoTelefonicoRepository.findByEliminadoFalse();
    }
}