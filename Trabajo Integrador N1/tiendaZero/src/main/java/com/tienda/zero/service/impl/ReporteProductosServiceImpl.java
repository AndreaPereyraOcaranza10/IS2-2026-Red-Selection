package com.tienda.zero.service.impl;

import com.tienda.zero.model.ContactoTelefonico;
import com.tienda.zero.model.OrdenCompraProveedor;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.Proveedor;
import com.tienda.zero.repository.OrdenCompraProveedorRepository;
import com.tienda.zero.repository.ProductoRepository;
import com.tienda.zero.service.ReporteProductosService;
import com.tienda.zero.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReporteProductosServiceImpl implements ReporteProductosService {

    private final ProductoRepository productoRepository;
    private final StockService stockService;
    private final OrdenCompraProveedorRepository ordenCompraProveedorRepository;

    @Override
    @Transactional(readOnly = true)
    public ReporteProductos generar() {
        Map<String, ProveedorContacto> proveedores = proveedoresPorProducto();
        List<ProductoStock> detalles = productoRepository.findByEliminadoFalse().stream()
                .map(producto -> construirDetalle(producto, proveedorPara(producto, proveedores)))
                .toList();

        int unidadesTotales = detalles.stream().mapToInt(ProductoStock::stockActual).sum();
        long productosConStock = detalles.stream().filter(item -> item.stockActual() > 0).count();
        long productosSinStockIdeal = detalles.stream().filter(item -> item.stockIdeal() <= 0).count();
        return new ReporteProductos(unidadesTotales, detalles.size(), (int) productosConStock,
                (int) productosSinStockIdeal, detalles);
    }

    private ProductoStock construirDetalle(Producto producto, ProveedorContacto proveedor) {
        int stockActual = stockService.calcularStockActual(producto.getId());
        int stockIdeal = producto.getStockIdeal();
        if (stockIdeal <= 0) {
            return new ProductoStock(producto.getId(), producto.getCodigo(), producto.getNombre(), stockActual,
                    stockIdeal, null, "Sin stock ideal", "bg-secondary-subtle text-secondary", 0, null, null);
        }

        double porcentaje = stockActual * 100.0 / stockIdeal;
        String estado;
        String claseEstado;
        if (porcentaje >= 50.0) {
            estado = "Bueno";
            claseEstado = "bg-success-subtle text-success";
        } else if (porcentaje >= 20.0) {
            estado = "Regular";
            claseEstado = "bg-warning-subtle text-warning-emphasis";
        } else {
            estado = "Malo";
            claseEstado = "bg-danger-subtle text-danger";
        }

        int objetivo50 = (stockIdeal + 1) / 2;
        int reposicion = Math.max(0, objetivo50 - stockActual);
        String whatsappUrl = null;
        String proveedorNombre = proveedor == null ? null : proveedor.nombre();
        if ("Malo".equals(estado) && reposicion > 0 && proveedor != null) {
            String telefono = normalizarTelefono(proveedor.telefono());
            if (telefono != null) {
                String mensaje = "Hola, necesitamos reponer " + reposicion + " unidades de " + producto.getNombre()
                        + " (codigo " + producto.getCodigo() + ") para alcanzar el 50% del stock ideal ("
                        + objetivo50 + " unidades). Podrian enviarlas?";
                whatsappUrl = "https://web.whatsapp.com/send?phone=" + telefono + "&text="
                        + URLEncoder.encode(mensaje, StandardCharsets.UTF_8);
            }
        }

        return new ProductoStock(producto.getId(), producto.getCodigo(), producto.getNombre(), stockActual,
                stockIdeal, Math.round(porcentaje * 10.0) / 10.0, estado, claseEstado, reposicion,
                proveedorNombre, whatsappUrl);
    }

    private Map<String, ProveedorContacto> proveedoresPorProducto() {
        Map<String, ProveedorContacto> proveedores = new HashMap<>();
        for (OrdenCompraProveedor orden : ordenCompraProveedorRepository.findAllByOrderByFechaCreacionDesc()) {
            Proveedor proveedor = orden.getProveedor();
            if (proveedor == null || proveedor.isEliminado()) continue;
            ProveedorContacto contactoProveedor = contactoDe(proveedor);
            if (orden.getDetalles() == null) continue;
            for (var detalle : orden.getDetalles()) {
                if (detalle.getProducto() != null) {
                    proveedores.putIfAbsent(detalle.getProducto().getId(), contactoProveedor);
                }
            }
        }
        return proveedores;
    }

    private ProveedorContacto contactoDe(Proveedor proveedor) {
        String telefono = proveedor.getContactos().stream()
                .filter(contacto -> !contacto.isEliminado() && contacto instanceof ContactoTelefonico)
                .map(contacto -> ((ContactoTelefonico) contacto).getTelefono())
                .filter(numero -> numero != null && !numero.isBlank())
                .findFirst().orElse(null);
        return new ProveedorContacto(proveedor.getRazonSocial(), telefono);
    }

    private ProveedorContacto proveedorPara(Producto producto, Map<String, ProveedorContacto> proveedores) {
        ProveedorContacto proveedorOrden = proveedores.get(producto.getId());
        if (proveedorOrden != null && proveedorOrden.telefono() != null) return proveedorOrden;
        if (producto.getProveedor() != null) return contactoDe(producto.getProveedor());
        return proveedorOrden;
    }

    private String normalizarTelefono(String telefono) {
        if (telefono == null) return null;
        String digitos = telefono.replaceAll("\\D", "");
        if (digitos.startsWith("00")) digitos = digitos.substring(2);
        if (!digitos.startsWith("54")) digitos = "549" + digitos;
        else if (!digitos.startsWith("549")) digitos = "549" + digitos.substring(2);
        return digitos.length() >= 11 ? digitos : null;
    }

    private record ProveedorContacto(String nombre, String telefono) {}
}
