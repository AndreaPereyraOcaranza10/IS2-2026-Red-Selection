package com.tienda.zero.service;

import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.model.Cliente;

import java.sql.Date;
import java.util.List;

public interface ClienteService {

    Cliente crearCliente(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                         String numeroDocumento, String telefono, String correoElectronico,
                         String direccionEstadia, String idNacionalidad);

    void validar(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                 String numeroDocumento, String telefono, String correoElectronico,
                 String direccionEstadia, String idNacionalidad);

    Cliente buscarCliente(String id);

    Cliente modificarCliente(String id, String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                             String numeroDocumento, String telefono, String correoElectronico,
                             String direccionEstadia, String idNacionalidad);

    void eliminarCliente(String id);

    List<Cliente> listarCliente();

    List<Cliente> listarClienteActivo();

}