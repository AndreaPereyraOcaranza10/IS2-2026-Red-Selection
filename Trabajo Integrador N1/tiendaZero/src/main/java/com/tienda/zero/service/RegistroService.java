package com.tienda.zero.service;

import com.tienda.zero.enums.Sexo;
import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.model.Cliente;
import com.tienda.zero.model.Usuario;

import java.sql.Date;

public interface RegistroService {

    Usuario registrarCliente(String correo, String clave);

    void activarCuentaCliente(String correo, String codigo);

    void reenviarCodigoActivacion(String correo);

    Cliente completarPerfilCliente(String idUsuario,
                                   String nombre, String apellido, Sexo sexo, Date fechaNacimiento,
                                   TipoDocumento tipoDocumento, String numeroDocumento,
                                   String idNacionalidad,
                                   String calle, String numeracion, String barrio, String manzanaPiso,
                                   String casaDepartamento, String referencia, String idLocalidad,
                                   String telefono);
}