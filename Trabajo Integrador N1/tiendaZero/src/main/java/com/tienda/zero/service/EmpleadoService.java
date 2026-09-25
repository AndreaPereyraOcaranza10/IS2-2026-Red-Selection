package com.tienda.zero.service;

import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoEmpleado;
import com.tienda.zero.model.Contacto;
import com.tienda.zero.model.Direccion;
import com.tienda.zero.model.Empleado;

import java.sql.Date;
import java.util.List;

public interface EmpleadoService {

    Empleado crearEmpleado(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                           String numeroDocumento, TipoEmpleado tipoEmpleado, String idEmpresa);

    void validar(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                 String numeroDocumento, TipoEmpleado tipoEmpleado, String idEmpresa);

    Empleado modificarEmpleado(String id, String nombre, String apellido, Date fechaNacimiento,
                               TipoDocumento tipoDocumento, String numeroDocumento,
                               TipoEmpleado tipoEmpleado, String idEmpresa);

    Empleado buscarEmpleado(String id);


    void eliminarEmpleado(String id);

    List<Empleado> listarEmpleado();

    List<Empleado> listarEmpleadoActivo();
}