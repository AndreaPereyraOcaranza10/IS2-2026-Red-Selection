package com.tienda.zero.service;

import com.tienda.zero.model.Proveedor;

import java.util.List;

public interface ProveedorService {

    Proveedor crearProveedor(String razonSocial, String cuit, String email,
                             String telefono, String idDireccion);

    void validar(String razonSocial, String cuit);

    Proveedor buscarProveedor(String id);

    Proveedor buscarProveedorPorCuit(String cuit);

    Proveedor modificarProveedor(String id, String razonSocial, String cuit, String email,
                                 String telefono, String idDireccion);

    void eliminarProveedor(String id);

    List<Proveedor> listarProveedor();

    List<Proveedor> listarProveedorActivo();
}
