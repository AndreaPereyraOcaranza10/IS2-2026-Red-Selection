package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.NotBlank;
import redSelection.dulceMayor.enums.Sexo;
import redSelection.dulceMayor.enums.TipoDocumento;
import redSelection.dulceMayor.enums.TipoTelefono;

/** Datos del perfil que completa el cliente en el portal. Las reglas de contenido las aplica PerfilClienteService. */
public record PerfilClienteDTO(
        @NotBlank String nombre,
        @NotBlank String apellido,
        Sexo sexo,
        @NotBlank String fechaNacimiento,
        TipoDocumento tipoDocumento,
        @NotBlank String numeroDocumento,
        @NotBlank String telefono,
        TipoTelefono tipoTelefono,
        @NotBlank String provinciaId,
        @NotBlank String departamentoId,
        @NotBlank String localidadId,
        @NotBlank String calle,
        @NotBlank String numeroCalle,
        String codigoPostal,
        String manzanaPiso,
        String casaDepartamento,
        String referencia
) { }