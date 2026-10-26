package com.tienda.zero.service;

import com.tienda.zero.enums.TipoContacto;
import com.tienda.zero.model.ContactoCorreoElectronico;

import java.util.List;

public interface ContactoCorreoElectronicoService {

    ContactoCorreoElectronico crearContactoCorreoElectronico(String email, TipoContacto tipoContacto,
                                                             String observacion);

    void validar(String email, TipoContacto tipoContacto, String observacion);

    void modificarContactoCorreoElectronico(String id, String email, TipoContacto tipoContacto, String observacion);

    List<ContactoCorreoElectronico> listarContactoCorreoElectronico();

    List<ContactoCorreoElectronico> listarContactoCorreoElectronicoActivo();
}