package com.tienda.zero.service;

import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoEmpleado;
import com.tienda.zero.model.Empleado;

import java.sql.Date;
import java.util.List;

public interface EmpleadoService {

    Empleado crearEmpleado(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                           String numeroDocumento, String telefono, String correoElectronico, TipoEmpleado tipoEmpleado);

    void validar(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                 String numeroDocumento, String telefono, String correoElectronico, TipoEmpleado tipoEmpleado);

    Empleado buscarEmpleado(String id);

    Empleado modificarEmpleado(String id, String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                               String numeroDocumento, TipoEmpleado tipoEmpleado);

    void eliminarEmpleado(String id);

    List<Empleado> listarEmpleado();

    List<Empleado> listarEmpleadoActivo();

}