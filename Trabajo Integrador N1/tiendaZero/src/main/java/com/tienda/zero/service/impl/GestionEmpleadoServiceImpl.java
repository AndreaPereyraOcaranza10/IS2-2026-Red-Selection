package com.tienda.zero.service.impl;

import com.tienda.zero.enums.Sexo;
import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoEmpleado;
import com.tienda.zero.enums.TipoUsuario;
import com.tienda.zero.model.Empleado;
import com.tienda.zero.model.Usuario;
import com.tienda.zero.service.EmpleadoService;
import com.tienda.zero.service.GestionEmpleadoService;
import com.tienda.zero.service.PersonaService;
import com.tienda.zero.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;

@Service
public class GestionEmpleadoServiceImpl implements GestionEmpleadoService {

    private final EmpleadoService empleadoService;
    private final UsuarioService usuarioService;
    private final PersonaService personaService;

    public GestionEmpleadoServiceImpl(EmpleadoService empleadoService,
                                      UsuarioService usuarioService,
                                      PersonaService personaService) {
        this.empleadoService = empleadoService;
        this.usuarioService = usuarioService;
        this.personaService = personaService;
    }

    @Override
    @Transactional
    public Empleado crearEmpleado(String nombre, String apellido, Sexo sexo, Date fechaNacimiento,
                                  TipoDocumento tipoDocumento, String numeroDocumento, TipoEmpleado tipoEmpleado,
                                  String idEmpresa, String nombreUsuario, String clave) {
        boolean quiereCuenta = hayTexto(nombreUsuario) || hayTexto(clave);
        exigirCuentaCompleta(quiereCuenta, nombreUsuario, clave);

        Empleado empleado = empleadoService.crearEmpleado(nombre, apellido, sexo, fechaNacimiento,
                tipoDocumento, numeroDocumento, tipoEmpleado, idEmpresa);

        if (quiereCuenta) {
            crearYAsociarCuenta(empleado.getId(), nombreUsuario, clave, tipoEmpleado);
        }
        return empleado;
    }

    @Override
    @Transactional
    public Empleado modificarEmpleado(String id, String nombre, String apellido, Sexo sexo, Date fechaNacimiento,
                                      TipoDocumento tipoDocumento, String numeroDocumento, TipoEmpleado tipoEmpleado,
                                      String idEmpresa, String nombreUsuario, String clave) {
        Empleado actual = empleadoService.buscarEmpleado(id);
        Usuario usuario = actual.getUsuario();
        TipoUsuario nuevoRol = rolDe(tipoEmpleado);
        boolean quiereCuenta = hayTexto(nombreUsuario) || hayTexto(clave);

        // Las reglas se comprueban antes de tocar nada
        if (usuario == null) {
            exigirCuentaCompleta(quiereCuenta, nombreUsuario, clave);
        } else if (usuario.getRol() == TipoUsuario.JEFE && nuevoRol != TipoUsuario.JEFE) {
            exigirOtroJefeActivo(usuario,
                    "No se puede cambiar el tipo del último jefe: nadie más podría administrar el sistema");
        }

        Empleado modificado = empleadoService.modificarEmpleado(id, nombre, apellido, sexo, fechaNacimiento,
                tipoDocumento, numeroDocumento, tipoEmpleado, idEmpresa);

        if (usuario == null) {
            if (quiereCuenta) {
                crearYAsociarCuenta(id, nombreUsuario, clave, tipoEmpleado);
            }
        } else {
            // El rol de la cuenta siempre sigue al tipo de empleado; la clave vacía conserva la actual
            String nombreFinal = hayTexto(nombreUsuario) ? nombreUsuario : usuario.getNombreUsuario();
            usuarioService.modificarUsuario(usuario.getId(), nombreFinal, clave, nuevoRol);
        }
        return modificado;
    }

    @Override
    @Transactional
    public void eliminarEmpleado(String id, String nombreUsuarioActual) {
        Empleado empleado = empleadoService.buscarEmpleado(id);
        Usuario usuario = empleado.getUsuario();

        if (usuario != null && !usuario.isEliminado()) {
            if (hayTexto(nombreUsuarioActual) && usuario.getNombreUsuario().equalsIgnoreCase(nombreUsuarioActual.trim())) {
                throw new IllegalArgumentException("No podés eliminar tu propio usuario");
            }
            if (usuario.getRol() == TipoUsuario.JEFE) {
                exigirOtroJefeActivo(usuario,
                        "No se puede eliminar al último jefe: nadie más podría administrar el sistema");
            }
        }

        empleadoService.eliminarEmpleado(id);
    }

    private void crearYAsociarCuenta(String idEmpleado, String nombreUsuario, String clave, TipoEmpleado tipoEmpleado) {
        Usuario usuario = usuarioService.crearUsuario(nombreUsuario, clave, rolDe(tipoEmpleado));
        personaService.asociarUsuarioPersona(idEmpleado, usuario.getId());
    }

    /** Usuario y clave se piden juntos: uno solo no alcanza para crear una cuenta. */
    private void exigirCuentaCompleta(boolean quiereCuenta, String nombreUsuario, String clave) {
        if (quiereCuenta && !(hayTexto(nombreUsuario) && hayTexto(clave))) {
            throw new IllegalArgumentException("Para crear la cuenta hacen falta el usuario y la clave");
        }
    }

    /** Falla si, sin este usuario, no quedaría ningún jefe activo que pueda iniciar sesión. */
    private void exigirOtroJefeActivo(Usuario usuario, String mensaje) {
        for (Usuario otro : usuarioService.listarUsuarioActivo()) {
            if (otro.getRol() == TipoUsuario.JEFE && otro.isCuentaActivada() && !otro.getId().equals(usuario.getId())) {
                return;
            }
        }
        throw new IllegalArgumentException(mensaje);
    }

    private TipoUsuario rolDe(TipoEmpleado tipoEmpleado) {
        if (tipoEmpleado == null) {
            throw new IllegalArgumentException("El tipo de empleado es obligatorio");
        }
        return TipoUsuario.valueOf(tipoEmpleado.name());
    }

    private boolean hayTexto(String texto) {
        return texto != null && !texto.isBlank();
    }
}