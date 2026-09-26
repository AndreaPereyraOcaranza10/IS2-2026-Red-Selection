package com.tienda.zero.service;

import com.tienda.zero.model.Contacto;
import com.tienda.zero.model.Proveedor;

import java.util.List;

public interface ProveedorService {

    Proveedor crearProveedor(String razonSocial, List<Contacto> contactos);

    void validar(String razonSocial);

    Proveedor buscarProveedor(String id);

    Proveedor buscarProveedorPorRazonSocial(String razonSocial);

    Proveedor modificarProveedor(String id, String razonSocial, List<Contacto> contactos);

    void eliminarProveedor(String id);

    List<Proveedor> listarProveedor();

    List<Proveedor> listarProveedorActivo();
}
