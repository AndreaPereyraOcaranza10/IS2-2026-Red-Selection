package com.tienda.zero.service.impl;

import com.tienda.zero.dto.ItemFacturaDTO;
import com.tienda.zero.enums.EstadoFactura;
import com.tienda.zero.model.DetalleOrdenCompraProveedor;
import com.tienda.zero.model.FacturaProveedor;
import com.tienda.zero.model.OrdenCompraProveedor;
import com.tienda.zero.service.FacturaProveedorService;
import com.tienda.zero.service.OrdenCompraProveedorService;
import com.tienda.zero.service.RecepcionMercaderiaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecepcionMercaderiaServiceImpl implements RecepcionMercaderiaService {

    private final OrdenCompraProveedorService ordenCompraProveedorService;
    private final FacturaProveedorService facturaProveedorService;

    public RecepcionMercaderiaServiceImpl(OrdenCompraProveedorService ordenCompraProveedorService,
                                          FacturaProveedorService facturaProveedorService) {
        this.ordenCompraProveedorService = ordenCompraProveedorService;
        this.facturaProveedorService = facturaProveedorService;
    }

    @Override
    @Transactional
    public FacturaProveedor recibirOrden(String idOrden, long numeroFactura, String idFormaDePago,
                                         EstadoFactura estado) {
        if (idOrden == null || idOrden.isBlank()) {
            throw new IllegalArgumentException("La orden de compra es obligatoria");
        }

        // buscarOrden devuelve null cuando no existe, así que se convierte en un error con mensaje
        OrdenCompraProveedor orden = ordenCompraProveedorService.buscarOrden(idOrden);
        if (orden == null) {
            throw new IllegalArgumentException("No existe la orden de compra con id: " + idOrden);
        }
        // Sin esta regla, recibir dos veces la misma orden sumaría el stock dos veces
        if (orden.isEntregada()) {
            throw new IllegalArgumentException("Esa orden de compra ya fue recibida");
        }

        List<ItemFacturaDTO> items = new ArrayList<>();
        for (DetalleOrdenCompraProveedor detalle : orden.getDetalles()) {
            if (detalle.getProducto() == null) {
                throw new IllegalArgumentException("La orden de compra tiene una línea sin producto");
            }
            items.add(new ItemFacturaDTO(detalle.getProducto().getId(), detalle.getCantidad(),
                    detalle.getPrecioUnitario()));
        }

        // La factura se crea con la fecha de hoy (cuando llega la mercadería) y queda vinculada a la orden
        Date hoy = Date.valueOf(LocalDate.now());
        FacturaProveedor factura = facturaProveedorService.crearFacturaProveedor(
                numeroFactura, hoy, estado, idFormaDePago, orden.getProveedor().getId(), idOrden, items);

        // Recién con la factura y el stock creados se marca la orden como entregada
        ordenCompraProveedorService.marcarEntregada(idOrden);
        return factura;
    }
}
