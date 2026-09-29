package com.tienda.zero.service;

import com.tienda.zero.enums.Sexo;
import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.model.Cliente;
import com.tienda.zero.model.Contacto;
import com.tienda.zero.model.Direccion;

import java.sql.Date;
import java.util.List;

public interface ClienteService {

    Cliente crearCliente(String nombre, String apellido, Sexo sexo, Date fechaNacimiento, TipoDocumento tipoDocumento,
                         String numeroDocumento, String idNacionalidad);

    void validar(String nombre, String apellido, Sexo sexo, Date fechaNacimiento, TipoDocumento tipoDocumento,
                 String numeroDocumento, String idNacionalidad);

    Cliente modificarCliente(String id, String nombre, String apellido, Sexo sexo, Date fechaNacimiento,
                             TipoDocumento tipoDocumento, String numeroDocumento,
                             String idNacionalidad);

    Cliente buscarCliente(String id);

    void eliminarCliente(String id);

    List<Cliente> listarCliente();

    List<Cliente> listarClienteActivo();
}