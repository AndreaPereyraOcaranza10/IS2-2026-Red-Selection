package com.tienda.zero.controller;

import com.tienda.zero.model.DetalleOrdenCompra;
import com.tienda.zero.model.OrdenCompra;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.Proveedor;
import com.tienda.zero.service.OrdenCompraService;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/ordenes")
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;
    private final ProveedorService proveedorService;
    private final ProductoService productoService;

    @GetMapping
    public String lista(Model model) {
        List<OrdenCompra> ordenes = ordenCompraService.listarOrdenes();
        model.addAttribute("ordenes", ordenes);
        return "admin/ordenes/lista";
    }

    @GetMapping("/nueva")
    public String nuevaForm(Model model) {
        List<Proveedor> proveedores = proveedorService.listarProveedorActivo();
        List<Producto> productos = productoService.listarProductoActivo();
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("productos", productos);
        model.addAttribute("orden", new OrdenCompra());
        return "admin/ordenes/formulario";
    }

    @PostMapping("/nueva")
    public String crearOrden(@ModelAttribute OrdenCompra ordenCompra,
                             @RequestParam(value = "productoId", required = false) List<String> productoIds,
                             @RequestParam(value = "cantidad", required = false) List<Integer> cantidades,
                             @RequestParam(value = "precioUnitario", required = false) List<Double> precios) {
        // Build detalles
        List<DetalleOrdenCompra> detalles = new ArrayList<>();
        if (productoIds != null) {
            for (int i = 0; i < productoIds.size(); i++) {
                String pid = productoIds.get(i);
                if (pid == null || pid.isBlank()) continue;
                Producto p = productoService.buscarProducto(pid);
                int qty = (cantidades != null && cantidades.size() > i && cantidades.get(i) != null) ? cantidades.get(i) : 1;
                double pu = (precios != null && precios.size() > i && precios.get(i) != null) ? precios.get(i) : 0.0;
                DetalleOrdenCompra d = DetalleOrdenCompra.builder().producto(p).cantidad(qty).precioUnitario(pu).build();
                detalles.add(d);
            }
        }
        ordenCompra.setDetalles(detalles);
        ordenCompraService.crearOrden(ordenCompra);
        return "redirect:/admin/ordenes";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable String id, Model model) {
        OrdenCompra o = ordenCompraService.buscarOrden(id);
        if (o == null) return "redirect:/admin/ordenes";
        model.addAttribute("orden", o);
        return "admin/ordenes/detalle";
    }

    @PostMapping("/{id}/entregar")
    public String marcarEntregada(@PathVariable String id) {
        ordenCompraService.marcarEntregada(id);
        return "redirect:/admin/ordenes";
    }
}
