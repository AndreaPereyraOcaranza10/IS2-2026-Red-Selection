package com.dulcemayor.service;

import com.dulcemayor.enums.TipoEmpresa;
import com.dulcemayor.model.Contacto;
import com.dulcemayor.model.Direccion;
import com.dulcemayor.model.Empresa;

import java.util.List;

public interface EmpresaService {

    Empresa crearEmpresa(String razonSocial, String cuit, TipoEmpresa tipoEmpresa,
                         Direccion direccion, Contacto contacto);

    void validar(String razonSocial, String cuit, TipoEmpresa tipoEmpresa,
                 Direccion direccion, Contacto contacto);

    Empresa buscarEmpresa(String id);

    Empresa buscarEmpresaPorNombre(String nombre);

    void modificarEmpresa(String id, String razonSocial, String cuit, TipoEmpresa tipoEmpresa,
                          Direccion direccion, Contacto contacto);

    void eliminarEmpresa(String id);

    List<Empresa> listarEmpresa();

    List<Empresa> listarEmpresaActiva();
}