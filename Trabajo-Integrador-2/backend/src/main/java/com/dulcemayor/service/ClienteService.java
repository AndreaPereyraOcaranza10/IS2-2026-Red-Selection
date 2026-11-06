package com.dulcemayor.service;

import com.dulcemayor.enums.TipoDocumento;
import com.dulcemayor.model.Cliente;
import com.dulcemayor.model.Contacto;
import com.dulcemayor.model.Direccion;

import java.sql.Date;
import java.util.List;

public interface ClienteService {

    Cliente crearCliente(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                         String numeroDocumento, String direccionEstadia, String idNacionalidad);

    void validar(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                 String numeroDocumento, String direccionEstadia, String idNacionalidad);

    Cliente modificarCliente(String id, String nombre, String apellido, Date fechaNacimiento,
                             TipoDocumento tipoDocumento, String numeroDocumento,
                             String direccionEstadia, String idNacionalidad);

    Cliente buscarCliente(String id);

    void eliminarCliente(String id);

    List<Cliente> listarCliente();

    List<Cliente> listarClienteActivo();
}