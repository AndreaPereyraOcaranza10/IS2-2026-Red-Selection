package com.tienda.zero.service.impl;

import com.tienda.zero.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación de EmailService.
 * Simula el envío de correos mostrando un recuadro formateado en consola
 * y almacenando el código en memoria para facilitar las pruebas locales.
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    // Almacena en memoria el último código generado por email para testing local
    private final Map<String, String> ultimosCodigos = new ConcurrentHashMap<>();

    @Override
    public void enviarCodigoActivacion(String destinatarioEmail, String codigoActivacion, String urlActivacion) {
        String emailNormalizado = destinatarioEmail.toLowerCase().trim();
        ultimosCodigos.put(emailNormalizado, codigoActivacion);

        String separador = "=".repeat(80);
        String mensaje = String.format("""
            \n%s
            📧 [TIENDA ZERO - CORREO DE ACTIVACIÓN ENVIADO]
            Para: %s
            Asunto: Activa tu cuenta en Tienda Zero
            
            Estimado/a cliente:
            ¡Gracias por registrarte en Tienda Zero!
            Para activar tu cuenta y poder iniciar sesión en el sistema, por favor
            ingresa a la siguiente página:
            
            👉 Página de activación: %s
            
            E ingresa el siguiente código de seguridad:
            🔑 CÓDIGO DE ACTIVACIÓN: %s
            
            Este código tiene una validez de 24 horas.
            Si no has solicitado este registro, puedes ignorar este correo.
            
            Atentamente,
            Equipo de Tienda Zero Mendoza
            %s
            """, separador, destinatarioEmail, urlActivacion, codigoActivacion, separador);

        System.out.println(mensaje);
        log.info("Simulación de correo: Código [{}] enviado a [{}]", codigoActivacion, destinatarioEmail);
    }

    @Override
    public String getUltimoCodigoSimulado(String email) {
        if (email == null) return null;
        return ultimosCodigos.get(email.toLowerCase().trim());
    }
}
