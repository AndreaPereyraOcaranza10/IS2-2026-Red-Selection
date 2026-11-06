package com.dulcemayor.service;

import com.dulcemayor.enums.TipoDocumento;
import com.dulcemayor.enums.TipoEmpleado;
import com.dulcemayor.model.Contacto;
import com.dulcemayor.model.Direccion;
import com.dulcemayor.model.Empleado;

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