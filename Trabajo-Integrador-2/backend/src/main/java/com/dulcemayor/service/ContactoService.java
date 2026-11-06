package com.dulcemayor.service;

import com.dulcemayor.enums.TipoContacto;
import com.dulcemayor.model.Contacto;

public interface ContactoService {

    void validar(TipoContacto tipoContacto, String observacion);

    Contacto buscarContacto(String idContacto);

    void eliminarContacto(String idContacto);
}