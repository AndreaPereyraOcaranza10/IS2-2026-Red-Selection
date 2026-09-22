package com.tienda.zero.service;

import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.model.Persona;

import java.sql.Date;
import java.util.List;

public interface PersonaService {

    Persona crearPersona(String nombre, String apellido, Date fechaNacimiento,
                         TipoDocumento tipoDocumento, String numeroDocumento);

    void validar(String nombre, String apellido, Date fechaNacimiento,
                 TipoDocumento tipoDocumento, String numeroDocumento);

    Persona buscarPersona(String id);

    Persona modificarPersona(String id, String nombre, String apellido, Date fechaNacimiento,
                             TipoDocumento tipoDocumento, String numeroDocumento);

    void eliminarPersona(String id);

    List<Persona> listarPersona();

    List<Persona> listarPersonaActivo();

    Persona asociarImagenPersona(String id, String idImagen);
}
