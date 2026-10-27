package com.tienda.zero.service;

import com.tienda.zero.enums.TipoContacto;
import com.tienda.zero.enums.TipoTelefono;
import com.tienda.zero.model.ContactoTelefonico;

import java.util.List;

public interface ContactoTelefonicoService {

    ContactoTelefonico crearContactoTelefonico(String telefono, TipoTelefono tipoTelefono,
                                               TipoContacto tipoContacto, String observacion);

    void validar(String telefono, TipoTelefono tipoTelefono, TipoContacto tipoContacto, String observacion);

    void modificarContactoTelefonico(String id, String telefono, TipoTelefono tipoTelefono, TipoContacto tipoContacto, String observacion);

    List<ContactoTelefonico> listarContactoTelefonico();

    List<ContactoTelefonico> listarContactoTelefonicoActivo();
}