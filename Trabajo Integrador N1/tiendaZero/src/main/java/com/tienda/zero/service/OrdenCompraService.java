package com.tienda.zero.service;

import com.tienda.zero.model.OrdenCompra;

import java.util.List;

public interface OrdenCompraService {
    OrdenCompra crearOrden(OrdenCompra ordenCompra);
    OrdenCompra buscarOrden(String id);
    List<OrdenCompra> listarOrdenes();
    List<OrdenCompra> listarOrdenesPendientes();
    OrdenCompra marcarEntregada(String id);
}
