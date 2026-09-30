package com.tienda.zero.service;

import com.tienda.zero.enums.EstadoOrdenCompra;
import com.tienda.zero.model.DetalleCompra;
import com.tienda.zero.model.OrdenCompra;
import com.tienda.zero.model.Producto;
import com.tienda.zero.repository.OrdenCompraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteVentasService {

    private final OrdenCompraRepository ordenCompraRepository;

    @Transactional(readOnly = true)
    public ReporteVentas generar(LocalDate desde, LocalDate hasta) {
        List<OrdenCompra> ordenes = ordenCompraRepository
                .findByFechaBetweenAndEstadoOrdenCompraNotInAndEliminadoFalseOrderByFechaDesc(
                        desde, hasta, List.of(EstadoOrdenCompra.PENDIENTE_COMPLETAR, EstadoOrdenCompra.ANULADA));

        BigDecimal totalVentas = ordenes.stream()
                .map(OrdenCompra::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int unidadesVendidas = ordenes.stream()
                .flatMap(orden -> orden.getDetalles().stream())
                .filter(detalle -> !detalle.isEliminado())
                .mapToInt(DetalleCompra::getCantidad)
                .sum();

        List<VentaDetalle> detalles = ordenes.stream()
                .flatMap(orden -> orden.getDetalles().stream()
                        .filter(detalle -> !detalle.isEliminado())
                        .map(detalle -> toDetalle(orden, detalle)))
                .toList();

        return new ReporteVentas(desde, hasta, ordenes.size(), unidadesVendidas, totalVentas, detalles);
    }

    private VentaDetalle toDetalle(OrdenCompra orden, DetalleCompra detalle) {
        Producto producto = detalle.getProducto();
        return new VentaDetalle(
                orden.getFecha(),
                producto.getNombre(),
                categoria(producto),
                detalle.getCantidad(),
                orden.getIdentificadorCompra(),
                orden.getFormaPago() != null ? orden.getFormaPago().name() : "No informada",
                detalle.getSubtotal()
        );
    }

    private String categoria(Producto producto) {
        if (producto.getSubCategoria() == null) {
            return "Sin categoria";
        }
        if (producto.getSubCategoria().getCategoria() == null) {
            return producto.getSubCategoria().getNombre();
        }
        return producto.getSubCategoria().getCategoria().getNombre();
    }

    public record ReporteVentas(LocalDate desde,
                                LocalDate hasta,
                                int cantidadOrdenes,
                                int unidadesVendidas,
                                BigDecimal totalVentas,
                                List<VentaDetalle> detalles) {
    }

    public record VentaDetalle(LocalDate fechaCompra,
                               String producto,
                               String categoria,
                               int cantidad,
                               String identificadorCompra,
                               String formaPago,
                               BigDecimal subtotal) {
    }
}
