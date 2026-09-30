package com.tienda.zero.service;

import com.tienda.zero.dto.EmpresaFormDTO;
import com.tienda.zero.model.Empresa;

public interface GestionEmpresaService {

    Empresa crearEmpresa(EmpresaFormDTO formulario);

    void modificarEmpresa(String id, EmpresaFormDTO formulario);

    void eliminarEmpresa(String id);
}
