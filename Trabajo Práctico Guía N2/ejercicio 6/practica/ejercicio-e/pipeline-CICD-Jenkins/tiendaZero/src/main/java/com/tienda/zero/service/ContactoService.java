package com.tienda.zero.service;

import com.tienda.zero.enums.TipoContacto;
import com.tienda.zero.model.Contacto;

public interface ContactoService {

    void validar(TipoContacto tipoContacto, String observacion);

    Contacto buscarContacto(String idContacto);

    void eliminarContacto(String idContacto);
}