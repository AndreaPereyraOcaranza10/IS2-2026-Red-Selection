package com.tienda.zero.service;

import com.tienda.zero.dto.ItemFacturaDTO;
import com.tienda.zero.model.OrdenCompraProveedor;

import java.util.List;

/**
 * Crea órdenes de compra a proveedores aplicando las reglas de negocio antes de guardarlas.
 * Arma la orden y delega el guardado en OrdenCompraProveedorService, sin modificarlo.
 */
public interface GestionOrdenProveedorService {

    /**
     * Crea una orden pendiente para el proveedor con las líneas indicadas (producto, cantidad y
     * precio de compra). Exige al menos una línea, cantidades y precios positivos, sin productos
     * repetidos, y que el proveedor y los productos no estén eliminados.
     */
    OrdenCompraProveedor crearOrden(String idProveedor, List<ItemFacturaDTO> items);
}