package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoContacto;
import com.tienda.zero.model.ContactoCorreoElectronico;
import com.tienda.zero.repository.ContactoCorreoElectronicoRepository;
import com.tienda.zero.service.ContactoCorreoElectronicoService;
import com.tienda.zero.service.ContactoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ContactoCorreoElectronicoServiceImpl implements ContactoCorreoElectronicoService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final ContactoCorreoElectronicoRepository contactoCorreoElectronicoRepository;
    private final ContactoService contactoService;

    @Override
    @Transactional
    public ContactoCorreoElectronico crearContactoCorreoElectronico(String email, TipoContacto tipoContacto,
                                                                    String observacion) {
        validar(email, tipoContacto, observacion);

        ContactoCorreoElectronico contacto = ContactoCorreoElectronico.builder()
                .email(email.trim())
                .tipoContacto(tipoContacto)
                .observacion(observacion)
                .build();

        return contactoCorreoElectronicoRepository.save(contacto);
    }

    @Override
    public void validar(String email, TipoContacto tipoContacto, String observacion) {
        contactoService.validar(tipoContacto, observacion);

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("El formato del email no es válido");
        }
    }

    @Override
    @Transactional
    public void modificarContactoCorreoElectronico(String id, String email, TipoContacto tipoContacto, String observacion) {
        ContactoCorreoElectronico contacto = contactoCorreoElectronicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el correo con id: " + id));

        if (contacto.isEliminado()) {
            throw new IllegalArgumentException("No se puede modificar un contacto eliminado");
        }

        validar(email, tipoContacto, observacion);

        contacto.setEmail(email.trim());
        contacto.setTipoContacto(tipoContacto);
        contacto.setObservacion(observacion);

        contactoCorreoElectronicoRepository.save(contacto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactoCorreoElectronico> listarContactoCorreoElectronico() {
        return contactoCorreoElectronicoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactoCorreoElectronico> listarContactoCorreoElectronicoActivo() {
        return contactoCorreoElectronicoRepository.findByEliminadoFalse();
    }
}