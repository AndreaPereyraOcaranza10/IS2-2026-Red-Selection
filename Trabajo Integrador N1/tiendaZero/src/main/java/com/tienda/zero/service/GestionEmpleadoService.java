package com.tienda.zero.service;

import com.tienda.zero.enums.Sexo;
import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoEmpleado;
import com.tienda.zero.model.Empleado;

import java.sql.Date;

/**
 * Altas, modificaciones y bajas de empleados junto con su cuenta de acceso, en una sola transacción.
 * Combina EmpleadoService y UsuarioService sin modificarlos, y aplica las reglas que ninguno de los
 * dos puede aplicar por separado: la cuenta se crea o no junto con el empleado, su rol sigue al tipo
 * de empleado, y el sistema nunca se queda sin jefe.
 */
public interface GestionEmpleadoService {

    /**
     * Crea el empleado y, si se indican usuario y clave, su cuenta con el rol del tipo de empleado.
     * Si algo falla no queda ni el empleado ni la cuenta. Usuario y clave se piden juntos o ninguno.
     */
    Empleado crearEmpleado(String nombre, String apellido, Sexo sexo, Date fechaNacimiento,
                           TipoDocumento tipoDocumento, String numeroDocumento, TipoEmpleado tipoEmpleado,
                           String idEmpresa, String nombreUsuario, String clave);

    /**
     * Modifica el empleado y mantiene su cuenta al día: si no tenía y se indican usuario y clave, la crea;
     * si ya tenía, ajusta su rol al nuevo tipo y, si se indican, cambia el usuario y la clave
     * (con la clave vacía se conserva la actual). No permite quitarle la jefatura al último jefe.
     */
    Empleado modificarEmpleado(String id, String nombre, String apellido, Sexo sexo, Date fechaNacimiento,
                               TipoDocumento tipoDocumento, String numeroDocumento, TipoEmpleado tipoEmpleado,
                               String idEmpresa, String nombreUsuario, String clave);

    /**
     * Da de baja al empleado y a su cuenta. No permite que alguien elimine su propio usuario
     * ni que se elimine al último jefe activo.
     *
     * @param nombreUsuarioActual usuario que está haciendo la baja (el que inició sesión)
     */
    void eliminarEmpleado(String id, String nombreUsuarioActual);
}