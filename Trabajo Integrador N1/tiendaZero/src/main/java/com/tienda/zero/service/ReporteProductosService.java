package com.tienda.zero.service;

import java.util.List;

public interface ReporteProductosService {

    ReporteProductos generar();

    record ReporteProductos(int unidadesTotales, int cantidadProductos,
                            int productosConStock, int productosSinStockIdeal,
                            List<ProductoStock> detalles) {}

    record ProductoStock(String id, String codigo, String nombre, int stockActual,
                         int stockIdeal, Double porcentaje, String estado, String claseEstado,
                         int unidadesParaLlegarAl50, String proveedor, String whatsappUrl) {}
}
