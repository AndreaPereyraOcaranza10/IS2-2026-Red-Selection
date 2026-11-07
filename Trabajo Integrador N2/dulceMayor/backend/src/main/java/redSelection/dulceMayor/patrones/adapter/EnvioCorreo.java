package redSelection.dulceMayor.patrones.adapter;

import redSelection.dulceMayor.patrones.builder.Notificacion;

/**
 * PATRÓN: Adapter (estructural) - interfaz destino (Target).
 * El sistema solo sabe "enviar una notificación". Cómo viaja (SMTP, un servicio transaccional, etc.) queda detrás.
 */
public interface EnvioCorreo {

    /** @return true si el correo salió; false si no hay servidor configurado o el envío falló. */
    boolean enviar(Notificacion notificacion);
}
