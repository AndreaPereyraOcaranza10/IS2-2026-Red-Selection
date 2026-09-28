package com.tienda.zero.dto;

import com.tienda.zero.enums.Sexo;
import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoTelefono;
import lombok.*;

/**
 * DTO para la captura y visualización de la información personal del perfil:
 * - Nombre, Apellido, Sexo, Fecha de Nacimiento
 * - Dirección (Provincia, Departamento, Localidad, Código Postal, Calle, N° Calle, Manzana/Piso, N° Casa/Departamento)
 * - Número de Teléfono
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilDTO {

    // 1. Datos Personales
    private String nombre;
    private String apellido;
    private Sexo sexo;
    private String fechaNacimiento; // En formato yyyy-MM-dd para input tipo date
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;

    // 2. Datos de Contacto
    private String email;
    private String telefono;
    private TipoTelefono tipoTelefono;

    // 3. Datos de Dirección
    private String provincia;
    private String departamento;
    private String localidad;
    private String codigoPostal;
    private String calle;
    private String numeroCalle;
    private String manzanaPiso;
    private String casaDepartamento;
    private String referencia;
}
