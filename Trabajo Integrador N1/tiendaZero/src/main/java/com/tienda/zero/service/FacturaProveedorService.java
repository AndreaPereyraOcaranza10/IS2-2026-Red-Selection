package com.tienda.zero.service;

import com.tienda.zero.dto.ItemFacturaDTO;
import com.tienda.zero.enums.EstadoFactura;
import com.tienda.zero.model.FacturaProveedor;

import java.sql.Date;
import java.util.List;

public interface FacturaProveedorService {

    /**
     * Registra una compra al proveedor: guarda la factura con sus líneas y suma al stock
     * cada producto. Todo o nada. El total se calcula a partir de las líneas.
     */
    FacturaProveedor crearFacturaProveedor(long numeroFactura, Date fechaFactura, EstadoFactura estado,
                                           String idFormaDePago, String idProveedor, List<ItemFacturaDTO> items);

    /**
     * Igual que la anterior, pero vinculando la factura a la orden de compra que la originó.
     * La orden tiene que existir, ser del mismo proveedor y no tener ya otra factura vigente.
     */
    FacturaProveedor crearFacturaProveedor(long numeroFactura, Date fechaFactura, EstadoFactura estado,
                                           String idFormaDePago, String idProveedor, String idOrdenCompra,
                                           List<ItemFacturaDTO> items);

    void validar(long numeroFactura, Date fechaFactura, EstadoFactura estado,
                 String idFormaDePago, String idProveedor, List<ItemFacturaDTO> items);

    FacturaProveedor buscarFacturaProveedor(String id);

    /**
     * Anula la factura y deshace la compra: descuenta del stock lo que había sumado y, si la factura
     * salió de una orden de compra, la orden vuelve a quedar pendiente. Falla si ese stock ya se vendió.
     */
    FacturaProveedor anularFacturaProveedor(String id, String observacion);

    List<FacturaProveedor> listarFacturaProveedor();
}