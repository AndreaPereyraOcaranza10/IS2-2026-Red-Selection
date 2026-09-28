package com.tienda.zero.dto;

import lombok.*;

/**
 * DTO para capturar los datos del formulario de registro de cliente:
 * Correo personal, contraseña y confirmación de contraseña.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroDTO {

    private String email;
    private String password;
    private String confirmPassword;
}
