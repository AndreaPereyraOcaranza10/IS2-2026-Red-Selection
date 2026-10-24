package com.tienda.zero.service;

import com.tienda.zero.enums.TipoEmpresa;
import com.tienda.zero.model.Contacto;
import com.tienda.zero.model.Direccion;
import com.tienda.zero.model.Empresa;

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