package com.tienda.zero.service;

/**
 * Servicio encargado de gestionar las notificaciones por correo electrónico,
 * en particular el envío del código y enlace de activación al registrarse un cliente.
 */
public interface EmailService {

    /**
     * Envía un correo con el código de activación y la URL de la página donde debe ingresarlo.
     *
     * @param destinatarioEmail Correo del cliente registrado.
     * @param codigoActivacion  Código de seguridad generado.
     * @param urlActivacion     URL hacia la página de activación.
     */
    void enviarCodigoActivacion(String destinatarioEmail, String codigoActivacion, String urlActivacion);

    /**
     * Retorna el último código generado para un correo (útil para pruebas y desarrollo local).
     */
    String getUltimoCodigoSimulado(String email);
}
