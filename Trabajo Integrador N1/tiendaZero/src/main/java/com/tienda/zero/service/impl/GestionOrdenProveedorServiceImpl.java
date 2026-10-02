package com.tienda.zero.service.impl;

import com.tienda.zero.dto.ItemFacturaDTO;
import com.tienda.zero.model.DetalleOrdenCompraProveedor;
import com.tienda.zero.model.OrdenCompraProveedor;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.Proveedor;
import com.tienda.zero.service.GestionOrdenProveedorService;
import com.tienda.zero.service.OrdenCompraProveedorService;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.ProveedorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GestionOrdenProveedorServiceImpl implements GestionOrdenProveedorService {

    private final OrdenCompraProveedorService ordenCompraProveedorService;
    private final ProveedorService proveedorService;
    private final ProductoService productoService;

    public GestionOrdenProveedorServiceImpl(OrdenCompraProveedorService ordenCompraProveedorService,
                                            ProveedorService proveedorService,
                                            ProductoService productoService) {
        this.ordenCompraProveedorService = ordenCompraProveedorService;
        this.proveedorService = proveedorService;
        this.productoService = productoService;
    }

    @Override
    @Transactional
    public OrdenCompraProveedor crearOrden(String idProveedor, List<ItemFacturaDTO> items) {
        validar(idProveedor, items);
        Proveedor proveedor = buscarProveedorActivo(idProveedor);

        OrdenCompraProveedor orden = new OrdenCompraProveedor();
        orden.setProveedor(proveedor);
        orden.setDetalles(armarDetalles(items));
        return ordenCompraProveedorService.crearOrden(orden);
    }

    @Override
    @Transactional
    public OrdenCompraProveedor modificarOrden(String idOrden, String idProveedor, List<ItemFacturaDTO> items) {
        OrdenCompraProveedor orden = ordenCompraProveedorService.buscarOrden(idOrden);
        if (orden == null) {
            throw new IllegalArgumentException("No existe la orden de compra con id: " + idOrden);
        }
        if (orden.isEntregada()) {
            throw new IllegalArgumentException("Una orden ya recibida no se puede modificar");
        }
        validar(idProveedor, items);
        Proveedor proveedor = buscarProveedorActivo(idProveedor);

        List<DetalleOrdenCompraProveedor> nuevos = armarDetalles(items);
        orden.setProveedor(proveedor);
        // Importante: con orphanRemoval hay que modificar la lista existente,
        // no reemplazarla por otra (setDetalles) o Hibernate lanza un error.
        orden.getDetalles().clear();
        orden.getDetalles().addAll(nuevos);
        return ordenCompraProveedorService.modificarOrden(orden);
    }

    private Proveedor buscarProveedorActivo(String idProveedor) {
        Proveedor proveedor = proveedorService.buscarProveedor(idProveedor);
        if (proveedor.isEliminado()) {
            throw new IllegalArgumentException("El proveedor está eliminado");
        }
        return proveedor;
    }

    private List<DetalleOrdenCompraProveedor> armarDetalles(List<ItemFacturaDTO> items) {
        List<DetalleOrdenCompraProveedor> detalles = new ArrayList<>();
        for (ItemFacturaDTO item : items) {
            Producto producto = productoService.buscarProducto(item.idProducto());
            if (producto.isEliminado()) {
                throw new IllegalArgumentException("El producto \"" + producto.getNombre() + "\" está eliminado");
            }
            DetalleOrdenCompraProveedor detalle = new DetalleOrdenCompraProveedor();
            detalle.setProducto(producto);
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(item.precioUnitario());
            detalles.add(detalle);
        }
        return detalles;
    }


    private void validar(String idProveedor, List<ItemFacturaDTO> items) {
        if (idProveedor == null || idProveedor.isBlank()) {
            throw new IllegalArgumentException("El proveedor es obligatorio");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La orden debe tener al menos un producto");
        }

        Set<String> productosVistos = new HashSet<>();
        for (ItemFacturaDTO item : items) {
            if (item == null || item.idProducto() == null || item.idProducto().isBlank()) {
                throw new IllegalArgumentException("Cada línea de la orden debe indicar un producto");
            }
            if (item.cantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de cada producto debe ser mayor a cero");
            }
            if (item.precioUnitario() <= 0) {
                throw new IllegalArgumentException("El precio de compra de cada producto debe ser mayor a cero");
            }
            if (!productosVistos.add(item.idProducto())) {
                throw new IllegalArgumentException("Un producto no puede aparecer dos veces en la misma orden");
            }
        }
    }

}

