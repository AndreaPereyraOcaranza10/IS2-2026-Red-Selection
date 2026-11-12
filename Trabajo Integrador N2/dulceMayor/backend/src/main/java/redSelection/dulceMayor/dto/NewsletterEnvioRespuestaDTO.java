package redSelection.dulceMayor.dto;

import java.time.LocalDate;

/** Resumen de un envío manual o programado de la newsletter mensual. */
public record NewsletterEnvioRespuestaDTO(
        LocalDate fecha,
        int productosIncluidos,
        int destinatarios,
        int enviados,
        int fallidos,
        int clientesSinCorreo
) { }
