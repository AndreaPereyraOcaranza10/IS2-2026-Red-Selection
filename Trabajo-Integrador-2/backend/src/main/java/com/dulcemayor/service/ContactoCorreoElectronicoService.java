package com.dulcemayor.service;

import com.dulcemayor.enums.TipoContacto;
import com.dulcemayor.model.ContactoCorreoElectronico;

import java.util.List;

public interface ContactoCorreoElectronicoService {

    ContactoCorreoElectronico crearContactoCorreoElectronico(String email, TipoContacto tipoContacto,
                                                             String observacion);

    void validar(String email, TipoContacto tipoContacto, String observacion);

    void modificarContactoCorreoElectronico(String id, String email, TipoContacto tipoContacto, String observacion);

    List<ContactoCorreoElectronico> listarContactoCorreoElectronico();

    List<ContactoCorreoElectronico> listarContactoCorreoElectronicoActivo();
}