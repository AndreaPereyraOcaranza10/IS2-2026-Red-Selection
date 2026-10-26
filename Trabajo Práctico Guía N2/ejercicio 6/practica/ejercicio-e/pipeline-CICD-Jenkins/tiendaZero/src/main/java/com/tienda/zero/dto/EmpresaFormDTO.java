package com.tienda.zero.dto;

import com.tienda.zero.enums.TipoContacto;
import com.tienda.zero.enums.TipoEmpresa;
import com.tienda.zero.enums.TipoTelefono;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpresaFormDTO {

    private String razonSocial;
    private String cuit;
    private TipoEmpresa tipoEmpresa;

    private String calle;
    private String numeracion;
    private String barrio;
    private String manzanaPiso;
    private String casaDepartamento;
    private String referencia;
    private String idProvincia;
    private String idDepartamento;
    private String idLocalidad;

    private String medioContacto = "CORREO";
    private TipoContacto tipoContacto = TipoContacto.EMPRESA;
    private String observacion;
    private String email;
    private String telefono;
    private TipoTelefono tipoTelefono = TipoTelefono.CELULAR;
}
