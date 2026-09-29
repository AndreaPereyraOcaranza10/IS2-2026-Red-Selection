package com.tienda.zero.controller;

import com.tienda.zero.enums.EstadoOrdenCompra;
import com.tienda.zero.model.*;
import com.tienda.zero.service.FlujoCompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController @RequiredArgsConstructor
public class FlujoCompraController {
    private final FlujoCompraService flujo;

    @GetMapping("/api/cart")
    public Map<String, Object> carrito(Authentication auth) { return flujo.verCarrito(auth.getName()); }

    @PostMapping("/api/cart/items/{productoId}")
    public Map<String, Object> agregar(@PathVariable String productoId, @RequestParam(defaultValue = "1") int cantidad, Authentication auth) {
        return flujo.agregarAlCarrito(auth.getName(), productoId, cantidad);
    }

    @PutMapping("/api/cart/items/{productoId}")
    public Map<String, Object> actualizar(@PathVariable String productoId, @RequestParam int cantidad, Authentication auth) {
        return flujo.actualizarCantidadCarrito(auth.getName(), productoId, cantidad);
    }

    @DeleteMapping("/api/cart/items/{productoId}")
    public Map<String, Object> quitar(@PathVariable String productoId, Authentication auth) {
        return flujo.quitarDelCarrito(auth.getName(), productoId);
    }

    @DeleteMapping("/api/cart")
    public Map<String, Object> vaciar(Authentication auth) {
        return flujo.vaciarCarrito(auth.getName());
    }

    @PostMapping("/api/orders")
    public OrdenCompra crearPedido(@RequestParam String direccionEntrega, Authentication auth) {
        return flujo.crearOrdenCliente(auth.getName(), direccionEntrega);
    }

    @GetMapping("/api/orders")
    public List<OrdenCompra> pedidos(Authentication auth) { return flujo.listarPedidosCliente(auth.getName()); }

    @PostMapping("/api/orders/{id}/cancel")
    public OrdenCompra cancelar(@PathVariable String id, Authentication auth) { return flujo.anularOrdenCliente(id, auth.getName()); }

    @PostMapping("/api/admin/purchase-orders")
    public OrdenCompraProveedor crearOrden(@RequestParam String proveedorId, @RequestParam String productoId, @RequestParam int cantidad, @RequestParam BigDecimal precioCompra) {
        return flujo.crearOrdenProveedor(proveedorId, productoId, cantidad, precioCompra);
    }

    @GetMapping("/api/admin/purchase-orders")
    public List<OrdenCompraProveedor> ordenesCompra() { return flujo.listarOrdenesProveedor(); }

    @PostMapping("/api/admin/purchase-orders/{id}/receive")
    public OrdenCompraProveedor recibir(@PathVariable String id) { return flujo.recibirOrdenProveedor(id); }

    @PostMapping("/api/admin/orders/{id}/tracking")
    public OrdenCompra seguimiento(@PathVariable String id, @RequestParam EstadoOrdenCompra estado) { return flujo.cambiarSeguimiento(id, estado); }

}
