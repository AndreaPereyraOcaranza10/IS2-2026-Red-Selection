package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.enums.TipoCorreo;

import java.util.List;

/**
 * Plantilla de un tipo de correo. `personalizada` = false significa que se está usando el texto por defecto
 * (o ninguno, si todavía no se envía ese tipo). `marcadores` son los {{campos}} que ese correo sabe completar.
 */
public record ConfiguracionCorreoRespuestaDTO(
        TipoCorreo tipo,
        String asunto,
        String cuerpoHtml,
        boolean personalizada,
        List<String> marcadores
) { }
