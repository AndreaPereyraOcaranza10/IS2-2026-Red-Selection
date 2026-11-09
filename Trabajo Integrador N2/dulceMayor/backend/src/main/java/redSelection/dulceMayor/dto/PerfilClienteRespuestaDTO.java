package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.enums.Sexo;
import redSelection.dulceMayor.enums.TipoDocumento;
import redSelection.dulceMayor.enums.TipoTelefono;

/** Perfil del cliente tal como lo muestra el portal. El correo es de solo lectura: es su usuario de acceso. */
public record PerfilClienteRespuestaDTO(
        String nombre, String apellido, Sexo sexo, String fechaNacimiento,
        TipoDocumento tipoDocumento, String numeroDocumento, String email,
        String telefono, TipoTelefono tipoTelefono, String provincia, String departamento,
        String localidad, String codigoPostal, String calle, String numeroCalle,
        String manzanaPiso, String casaDepartamento, String referencia,
        String provinciaId, String departamentoId, String localidadId
) {
    /** Cuenta recién creada que todavía no completó su perfil. */
    public static PerfilClienteRespuestaDTO vacio(String email) {
        return new PerfilClienteRespuestaDTO("", "", null, "", TipoDocumento.DNI, "", email, "",
                TipoTelefono.CELULAR, "", "", "", "", "", "", "", "", "", "", "", "");
    }
}