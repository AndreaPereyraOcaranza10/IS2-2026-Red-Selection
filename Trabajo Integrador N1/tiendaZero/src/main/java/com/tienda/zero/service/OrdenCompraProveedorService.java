package com.tienda.zero.service;

import com.tienda.zero.model.OrdenCompraProveedor;

import java.util.List;

public interface OrdenCompraProveedorService {
    OrdenCompraProveedor crearOrden(OrdenCompraProveedor ordenCompraProveedor);
    OrdenCompraProveedor buscarOrden(String id);
    List<OrdenCompraProveedor> listarOrdenes();
    List<OrdenCompraProveedor> listarOrdenesPendientes();
    OrdenCompraProveedor marcarEntregada(String id);
}
