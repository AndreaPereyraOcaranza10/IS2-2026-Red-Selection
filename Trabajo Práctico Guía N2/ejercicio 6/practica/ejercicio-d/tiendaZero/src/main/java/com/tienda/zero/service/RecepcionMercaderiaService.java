package com.tienda.zero.service;

import com.tienda.zero.enums.EstadoFactura;
import com.tienda.zero.model.FacturaProveedor;

/**
 * Recepción de una orden de compra al proveedor: cuando llega la mercadería se registra la
 * factura del proveedor (que suma el stock) y la orden queda marcada como entregada.
 * Todo en una sola transacción, para que nunca haya una orden entregada sin stock ni al revés.
 */
public interface RecepcionMercaderiaService {

    /**
     * Recibe la orden: crea la factura del proveedor con las líneas de la orden (mismo producto,
     * cantidad y precio de compra), suma el stock y marca la orden como entregada.
     *
     * @param numeroFactura número de la factura que trae el proveedor
     * @param idFormaDePago cómo se paga esa factura
     * @param estado        estado de la factura (por ejemplo PAGADA o SIN_DEFINIR); el stock suma en ambos
     */
    FacturaProveedor recibirOrden(String idOrden, long numeroFactura, String idFormaDePago, EstadoFactura estado);
}