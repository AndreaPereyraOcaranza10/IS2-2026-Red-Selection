package com.tienda.zero.dto;

import com.tienda.zero.enums.Sexo;
import com.tienda.zero.enums.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilClienteDTO {

    private String correo;
    private String nombre;
    private String apellido;
    private Sexo sexo;
    private String fechaNacimiento;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String telefono;
    private String idNacionalidad;
    private String direccionEstadia;
    private String idProvincia;
    private String idDepartamento;
    private String idLocalidad;
    private String calle;
    private String numeracion;
    private String barrio;
    private String manzanaPiso;
    private String casaDepartamento;
    private String referencia;
}
