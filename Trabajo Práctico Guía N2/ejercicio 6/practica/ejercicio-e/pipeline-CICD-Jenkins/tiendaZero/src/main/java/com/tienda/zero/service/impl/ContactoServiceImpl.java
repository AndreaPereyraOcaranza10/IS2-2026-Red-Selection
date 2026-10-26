package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoContacto;
import com.tienda.zero.model.Contacto;
import com.tienda.zero.repository.ContactoRepository;
import com.tienda.zero.service.ContactoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactoServiceImpl implements ContactoService {

    private final ContactoRepository contactoRepository;

    @Override
    public void validar(TipoContacto tipoContacto, String observacion) {
        if (tipoContacto == null) {
            throw new IllegalArgumentException("El tipo de contacto es obligatorio");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Contacto buscarContacto(String idContacto) {
        return contactoRepository.findById(idContacto)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el contacto con id: " + idContacto));
    }

    @Override
    @Transactional
    public void eliminarContacto(String idContacto) {
        Contacto contacto = buscarContacto(idContacto);
        contacto.setEliminado(true);
        contactoRepository.save(contacto);
    }
}