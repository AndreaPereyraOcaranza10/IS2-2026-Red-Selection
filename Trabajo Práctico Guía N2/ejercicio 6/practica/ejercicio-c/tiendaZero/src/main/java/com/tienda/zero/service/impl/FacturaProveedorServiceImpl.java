package com.tienda.zero.service.impl;

import com.tienda.zero.dto.ItemFacturaDTO;
import com.tienda.zero.enums.EstadoFactura;
import com.tienda.zero.model.DetalleFactura;
import com.tienda.zero.model.FacturaProveedor;
import com.tienda.zero.model.FormaDePago;
import com.tienda.zero.model.OrdenCompraProveedor;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.Proveedor;
import com.tienda.zero.repository.FacturaProveedorRepository;
import com.tienda.zero.service.FacturaProveedorService;
import com.tienda.zero.service.FormaDePagoService;
import com.tienda.zero.service.OrdenCompraProveedorService;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.ProveedorService;
import com.tienda.zero.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class FacturaProveedorServiceImpl implements FacturaProveedorService {

    private final FacturaProveedorRepository facturaProveedorRepository;
    private final ProveedorService proveedorService;
    private final FormaDePagoService formaDePagoService;
    private final ProductoService productoService;
    private final StockService stockService;
    private final OrdenCompraProveedorService ordenCompraProveedorService;

    public FacturaProveedorServiceImpl(FacturaProveedorRepository facturaProveedorRepository,
                                       ProveedorService proveedorService,
                                       FormaDePagoService formaDePagoService,
                                       ProductoService productoService,
                                       StockService stockService,
                                       OrdenCompraProveedorService ordenCompraProveedorService) {
        this.facturaProveedorRepository = facturaProveedorRepository;
        this.proveedorService = proveedorService;
        this.formaDePagoService = formaDePagoService;
        this.productoService = productoService;
        this.stockService = stockService;
        this.ordenCompraProveedorService = ordenCompraProveedorService;
    }

    @Override
    @Transactional
    public FacturaProveedor crearFacturaProveedor(long numeroFactura, Date fechaFactura, EstadoFactura estado,
                                                  String idFormaDePago, String idProveedor,
                                                  List<ItemFacturaDTO> items) {
        // Factura cargada sin orden de compra previa
        return crearFacturaProveedor(numeroFactura, fechaFactura, estado, idFormaDePago, idProveedor, null, items);
    }

    @Override
    @Transactional
    public FacturaProveedor crearFacturaProveedor(long numeroFactura, Date fechaFactura, EstadoFactura estado,
                                                  String idFormaDePago, String idProveedor, String idOrdenCompra,
                                                  List<ItemFacturaDTO> items) {
        validar(numeroFactura, fechaFactura, estado, idFormaDePago, idProveedor, items);
        OrdenCompraProveedor orden = buscarOrdenParaFactura(idOrdenCompra, idProveedor);

        Proveedor proveedor = proveedorService.buscarProveedor(idProveedor);
        if (proveedor.isEliminado()) {
            throw new IllegalArgumentException("El proveedor está eliminado");
        }
        FormaDePago formaDePago = formaDePagoService.buscarFormaDePago(idFormaDePago);
        if (formaDePago.isEliminado()) {
            throw new IllegalArgumentException("La forma de pago está eliminada");
        }

        FacturaProveedor factura = new FacturaProveedor();
        factura.setNumeroFactura(numeroFactura);
        factura.setFechaFactura(fechaFactura);
        factura.setEstado(estado);
        factura.setEliminado(false);
        factura.setProveedor(proveedor);
        factura.setFormaDePago(formaDePago);
        factura.setOrdenCompra(orden);
        factura.setDetalles(new ArrayList<>());

        double total = 0;
        for (ItemFacturaDTO item : items) {
            Producto producto = productoService.buscarProducto(item.idProducto());
            if (producto.isEliminado()) {
                throw new IllegalArgumentException("El producto \"" + producto.getNombre() + "\" está eliminado");
            }
            double subtotal = redondear(item.cantidad() * item.precioUnitario());

            DetalleFactura detalle = new DetalleFactura();
            detalle.setCantidad(item.cantidad());
            detalle.setSubtotal(subtotal);
            detalle.setEliminado(false);
            detalle.setProducto(producto);
            factura.getDetalles().add(detalle);

            total += subtotal;
        }
        factura.setTotalPagado(redondear(total));

        FacturaProveedor guardada = facturaProveedorRepository.save(factura);
        for (DetalleFactura detalle : guardada.getDetalles()) {
            stockService.crearStock(detalle.getId());
        }
        return guardada;
    }

    @Override
    public void validar(long numeroFactura, Date fechaFactura, EstadoFactura estado,
                        String idFormaDePago, String idProveedor, List<ItemFacturaDTO> items) {
        if (numeroFactura <= 0) {
            throw new IllegalArgumentException("El número de factura debe ser mayor a cero");
        }
        if (fechaFactura == null) {
            throw new IllegalArgumentException("La fecha de la factura es obligatoria");
        }
        if (fechaFactura.toLocalDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de la factura no puede ser futura");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado de la factura es obligatorio");
        }
        if (estado == EstadoFactura.ANULADA) {
            throw new IllegalArgumentException("No se puede crear una factura ya anulada");
        }
        if (idProveedor == null || idProveedor.isBlank()) {
            throw new IllegalArgumentException("El proveedor es obligatorio");
        }
        if (idFormaDePago == null || idFormaDePago.isBlank()) {
            throw new IllegalArgumentException("La forma de pago es obligatoria");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La factura debe tener al menos un producto");
        }

        Set<String> productosVistos = new HashSet<>();
        for (ItemFacturaDTO item : items) {
            if (item == null || item.idProducto() == null || item.idProducto().isBlank()) {
                throw new IllegalArgumentException("Cada línea de la factura debe indicar un producto");
            }
            if (item.cantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de cada producto debe ser mayor a cero");
            }
            if (item.precioUnitario() <= 0) {
                throw new IllegalArgumentException("El precio de compra de cada producto debe ser mayor a cero");
            }
            if (!productosVistos.add(item.idProducto())) {
                throw new IllegalArgumentException("Un producto no puede aparecer dos veces en la misma factura");
            }
        }

        if (facturaProveedorRepository.existsByProveedorIdAndNumeroFacturaAndEstadoNotAndEliminadoFalse(
                idProveedor, numeroFactura, EstadoFactura.ANULADA)) {
            throw new IllegalArgumentException("Ya existe una factura N° " + numeroFactura + " de ese proveedor");
        }
    }

    @Override
    public FacturaProveedor buscarFacturaProveedor(String id) {
        Optional<FacturaProveedor> resultado = facturaProveedorRepository.findById(id);
        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("No existe la factura de proveedor con id: " + id);
        }
        return resultado.get();
    }

    @Override
    @Transactional
    public FacturaProveedor anularFacturaProveedor(String id, String observacion) {
        FacturaProveedor factura = buscarFacturaProveedor(id);
        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new IllegalArgumentException("La factura ya está anulada");
        }

        // Si el stock de alguna línea ya se vendió, esto falla y la transacción deshace todo
        for (DetalleFactura detalle : factura.getDetalles()) {
            stockService.revertirStock(detalle.getId(), observacion);
        }

        factura.setEstado(EstadoFactura.ANULADA);
        FacturaProveedor anulada = facturaProveedorRepository.save(factura);

        // Anular la compra la deshace por completo: la orden que la originó vuelve a estar pendiente
        if (anulada.getOrdenCompra() != null) {
            ordenCompraProveedorService.marcarPendiente(anulada.getOrdenCompra().getId());
        }
        return anulada;
    }

    @Override
    public List<FacturaProveedor> listarFacturaProveedor() {
        return facturaProveedorRepository.findByEliminadoFalseOrderByFechaFacturaDesc();
    }

    /** Devuelve la orden a vincular (null si la factura no viene de una orden), validando que corresponda. */
    private OrdenCompraProveedor buscarOrdenParaFactura(String idOrdenCompra, String idProveedor) {
        if (idOrdenCompra == null || idOrdenCompra.isBlank()) {
            return null;
        }
        OrdenCompraProveedor orden = ordenCompraProveedorService.buscarOrden(idOrdenCompra);
        if (orden == null) {
            throw new IllegalArgumentException("No existe la orden de compra con id: " + idOrdenCompra);
        }
        if (!orden.getProveedor().getId().equals(idProveedor)) {
            throw new IllegalArgumentException("La orden de compra es de otro proveedor");
        }
        if (facturaProveedorRepository.existsByOrdenCompraIdAndEstadoNotAndEliminadoFalse(
                idOrdenCompra, EstadoFactura.ANULADA)) {
            throw new IllegalArgumentException("Esa orden de compra ya tiene una factura");
        }
        return orden;
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}