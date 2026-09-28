package com.tienda.zero.service;

import com.tienda.zero.dto.RegistroDTO;
import com.tienda.zero.model.Usuario;

/**
 * Servicio de negocio para la gestión de usuarios, registro y activación de cuentas.
 */
public interface UsuarioService {

    /**
     * Registra un nuevo cliente en el sistema en estado inactivo y envía el código de activación.
     *
     * @param registroDTO Datos del formulario (email y contraseñas).
     * @param baseUrl     URL base de la aplicación para componer el enlace de activación.
     * @return El usuario registrado.
     */
    Usuario registrarCliente(RegistroDTO registroDTO, String baseUrl);

    /**
     * Valida el código de activación para el correo dado y pasa la cuenta a activa.
     *
     * @param email  Correo personal del usuario.
     * @param codigo Código de 6 dígitos ingresado por el usuario.
     * @return true si la activación fue exitosa.
     */
    boolean activarCuenta(String email, String codigo);

    /**
     * Genera un nuevo código de activación y lo reenvía al correo del usuario.
     *
     * @param email   Correo del usuario.
     * @param baseUrl URL base de la aplicación.
     */
    void reenviarCodigoActivacion(String email, String baseUrl);

    /**
     * Busca un usuario por su correo electrónico.
     */
    Usuario buscarPorEmail(String email);

    /**
     * Guarda o actualiza un usuario en la base de datos.
     */
    Usuario guardar(Usuario usuario);
}
