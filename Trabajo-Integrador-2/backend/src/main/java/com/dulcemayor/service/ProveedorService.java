package com.dulcemayor.service;

import com.dulcemayor.model.Contacto;
import com.dulcemayor.model.Proveedor;

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
