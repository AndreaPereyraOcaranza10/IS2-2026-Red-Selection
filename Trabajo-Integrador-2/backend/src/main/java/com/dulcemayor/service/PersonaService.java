package com.dulcemayor.service;

import com.dulcemayor.enums.TipoDocumento;
import com.dulcemayor.model.Contacto;
import com.dulcemayor.model.Direccion;
import com.dulcemayor.model.Persona;

import java.sql.Date;
import java.util.List;

public interface PersonaService {

    Persona buscarPersona(String id);

    void eliminarPersona(String id);


    void validar(String nombre, String apellido, Date fechaNacimiento,
                 TipoDocumento tipoDocumento, String numeroDocumento);


    void validarParaModificar(String id, String nombre, String apellido, Date fechaNacimiento,
                              TipoDocumento tipoDocumento, String numeroDocumento);

    Persona asociarImagenPersona(String id, String idImagen);

    //Persona asociarUsuarioPersona(String id, String idContacto);

    Persona asociarContactoPersona(String id, String idContacto);

    Persona asociarDireccionPersona(String id, String idDireccion);
}
