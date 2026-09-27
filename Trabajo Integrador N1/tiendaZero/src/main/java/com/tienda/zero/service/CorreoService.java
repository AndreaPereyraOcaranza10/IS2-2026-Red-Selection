package com.tienda.zero.service;

public interface CorreoService {
    void enviarCorreoHtml(String destinatario, String asunto, String cuerpoHtml);
}