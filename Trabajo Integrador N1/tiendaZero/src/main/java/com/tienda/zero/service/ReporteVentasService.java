package com.tienda.zero.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ReporteVentasService {

    ReporteVentas generar(LocalDate desde, LocalDate hasta);

    record ReporteVentas(LocalDate desde,
                         LocalDate hasta,
                         int cantidadOrdenes,
                         int unidadesVendidas,
                         BigDecimal totalVentas,
                         List<VentaDetalle> detalles) {
    }

    record VentaDetalle(LocalDate fechaCompra,
                        String producto,
                        String categoria,
                        int cantidad,
                        String identificadorCompra,
                        String formaPago,
                        BigDecimal subtotal) {
    }
}
