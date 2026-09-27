package com.tienda.zero.service;

import com.tienda.zero.enums.Sexo;
import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.model.Contacto;
import com.tienda.zero.model.Direccion;
import com.tienda.zero.model.Persona;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public interface PersonaService {

    Persona buscarPersona(String id);

    void eliminarPersona(String id);


    void validar(String nombre, String apellido, Sexo sexo, Date fechaNacimiento,
                 TipoDocumento tipoDocumento, String numeroDocumento);

    Optional<Persona> buscarPersonaPorUsuario(String idUsuario);

    //void validarParaModificar(String id, String nombre, String apellido, Sexo sexo, Date fechaNacimiento,
                              //TipoDocumento tipoDocumento, String numeroDocumento);

    Persona asociarImagenPersona(String id, String idImagen);

    Persona asociarContactoPersona(String id, String idContacto);

    Persona asociarDireccionPersona(String id, String idDireccion);

    Persona asociarUsuarioPersona(String id, String idUsuario);
}
