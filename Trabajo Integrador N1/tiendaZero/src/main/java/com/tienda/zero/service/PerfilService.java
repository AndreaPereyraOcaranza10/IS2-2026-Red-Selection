package com.tienda.zero.service;

import com.tienda.zero.dto.PerfilDTO;

/**
 * Servicio encargado de gestionar la información personal del perfil:
 * Nombre, Apellido, Sexo, Fecha de Nacimiento, Dirección completa y Teléfono.
 */
public interface PerfilService {

    /**
     * Obtiene los datos del perfil para el usuario indicado por su email.
     */
    PerfilDTO obtenerPerfilUsuario(String email);

    /**
     * Registra o actualiza la información personal del usuario, vinculando la Persona/Cliente
     * con su dirección y medios de contacto correspondientes.
     */
    void guardarPerfilUsuario(String email, PerfilDTO perfilDTO);

    /**
     * Comprueba si el usuario ya ha completado su información personal básica.
     */
    boolean tienePerfilCompleto(String email);
}
