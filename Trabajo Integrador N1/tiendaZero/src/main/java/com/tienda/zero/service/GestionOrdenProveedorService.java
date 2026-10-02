package com.tienda.zero.service;

import com.tienda.zero.dto.ItemFacturaDTO;
import com.tienda.zero.model.OrdenCompraProveedor;

import java.util.List;

/**
 * Crea órdenes de compra a proveedores aplicando las reglas de negocio antes de guardarlas.
 * Arma la orden y delega el guardado en OrdenCompraProveedorService, sin modificarlo.
 */
public interface GestionOrdenProveedorService {
    OrdenCompraProveedor crearOrden(String idProveedor, List<ItemFacturaDTO> items);
    OrdenCompraProveedor modificarOrden(String idOrden, String idProveedor, List<ItemFacturaDTO> items);
}