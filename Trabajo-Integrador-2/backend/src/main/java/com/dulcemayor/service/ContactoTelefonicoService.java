package com.dulcemayor.service;

import com.dulcemayor.enums.TipoContacto;
import com.dulcemayor.enums.TipoTelefono;
import com.dulcemayor.model.ContactoTelefonico;

import java.util.List;

public interface ContactoTelefonicoService {

    ContactoTelefonico crearContactoTelefonico(String telefono, TipoTelefono tipoTelefono,
                                               TipoContacto tipoContacto, String observacion);

    void validar(String telefono, TipoTelefono tipoTelefono, TipoContacto tipoContacto, String observacion);

    void modificarContactoTelefonico(String id, String telefono, TipoTelefono tipoTelefono, TipoContacto tipoContacto, String observacion);

    List<ContactoTelefonico> listarContactoTelefonico();

    List<ContactoTelefonico> listarContactoTelefonicoActivo();
}