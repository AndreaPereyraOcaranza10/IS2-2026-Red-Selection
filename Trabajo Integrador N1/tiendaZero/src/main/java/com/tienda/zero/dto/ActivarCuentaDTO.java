package com.tienda.zero.dto;

import lombok.*;

/**
 * DTO para la página de activación:
 * Recibe el correo del usuario y el código de activación enviado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivarCuentaDTO {

    private String email;
    private String codigo;
}
