// dto/SucursalSolicitudDTO.java
package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Alta / modificación de una sucursal (los nombres de campo son los que ya consume el front). */
public record SucursalSolicitudDTO(@NotBlank String razonSocial, @NotBlank String cuit,
                                   @NotBlank String calle, @NotBlank String numeracion,
                                   String barrio, String manzanaPiso, String casaDepartamento, String referencia,
                                   @NotBlank String provinciaId, @NotBlank String departamentoId,
                                   @NotBlank String localidadId, @Email @NotBlank String email) { }