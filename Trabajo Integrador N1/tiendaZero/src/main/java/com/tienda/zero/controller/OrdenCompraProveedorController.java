package com.tienda.zero.controller;

import com.tienda.zero.dto.ItemFacturaDTO;
import com.tienda.zero.enums.EstadoFactura;
import com.tienda.zero.enums.EstadoOrdenCompra;
import com.tienda.zero.model.DetalleOrdenCompraProveedor;
import com.tienda.zero.model.FacturaProveedor;
import com.tienda.zero.model.OrdenCompraProveedor;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.Proveedor;
import com.tienda.zero.service.FormaDePagoService;
import com.tienda.zero.service.FlujoCompraService;
import com.tienda.zero.service.GestionOrdenProveedorService;
import com.tienda.zero.service.OrdenCompraProveedorService;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.ProveedorService;
import com.tienda.zero.service.RecepcionMercaderiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/ordenes")
public class OrdenCompraProveedorController {

    private final OrdenCompraProveedorService ordenCompraProveedorService;
    private final GestionOrdenProveedorService gestionOrdenProveedorService;
    private final RecepcionMercaderiaService recepcionMercaderiaService;
    private final ProveedorService proveedorService;
    private final ProductoService productoService;
    private final FormaDePagoService formaDePagoService;
    private final FlujoCompraService flujoCompraService;

    @GetMapping
    public String lista(Model model) {
        List<OrdenCompraProveedor> ordenes = ordenCompraProveedorService.listarOrdenes();
        model.addAttribute("ordenes", ordenes);
        var pedidos = flujoCompraService.listarPedidosAdministracion();
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("estadosPedidoDisponibles", flujoCompraService.estadosAdministracion());
        return "admin/ordenes/lista";
    }

    @PostMapping("/clientes/{id}/estado")
    public String actualizarPedidoCliente(@PathVariable String id,
                                          @RequestParam EstadoOrdenCompra estado,
                                          RedirectAttributes redirectAttributes) {
        try {
            flujoCompraService.cambiarEstadoAdministracion(id, estado);
            redirectAttributes.addFlashAttribute("mensajeExito", "El estado del pedido fue actualizado.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/ordenes";
    }

    @PostMapping("/clientes/{id}/seguimiento")
    public String actualizarSeguimientoCliente(@PathVariable String id,
                                               @RequestParam EstadoOrdenCompra estado,
                                               RedirectAttributes redirectAttributes) {
        try {
            flujoCompraService.cambiarSeguimiento(id, estado);
            redirectAttributes.addFlashAttribute("mensajeExito", "El seguimiento del pedido fue actualizado.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/ordenes";
    }

    @GetMapping("/nueva")
    public String nuevaForm(Model model) {
        List<Proveedor> proveedores = proveedorService.listarProveedorActivo();
        List<Producto> productos = productoService.listarProductoActivo();
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("productos", productos);
        model.addAttribute("orden", new OrdenCompraProveedor());
        return "admin/ordenes/formulario";
    }

    /**
     * El alta pasa por GestionOrdenProveedorService, que valida antes de guardar:
     * al menos una línea, cantidades y precios positivos, sin productos repetidos y sin
     * proveedor ni productos eliminados. Si algo falla, vuelve al formulario con el motivo.
     */
    @PostMapping("/nueva")
    public String crearOrden(@ModelAttribute OrdenCompraProveedor ordenCompraProveedor,
                             @RequestParam(value = "productoId", required = false) List<String> productoIds,
                             @RequestParam(value = "cantidad", required = false) List<Integer> cantidades,
                             @RequestParam(value = "precioUnitario", required = false) List<Double> precios,
                             RedirectAttributes redirectAttributes) {
        String idProveedor = ordenCompraProveedor.getProveedor() != null
                ? ordenCompraProveedor.getProveedor().getId() : null;
        try {
            gestionOrdenProveedorService.crearOrden(idProveedor, armarItems(productoIds, cantidades, precios));
            redirectAttributes.addFlashAttribute("mensajeExito", "La orden de compra fue creada.");
            return "redirect:/admin/ordenes";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/ordenes/nueva";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable String id, Model model) {
        OrdenCompraProveedor o = ordenCompraProveedorService.buscarOrden(id);
        if (o == null) return "redirect:/admin/ordenes";
        model.addAttribute("orden", o);
        model.addAttribute("total", calcularTotal(o));
        model.addAttribute("formasDePago", formaDePagoService.listarFormaDePagoActivo());
        model.addAttribute("estados", List.of(EstadoFactura.PAGADA, EstadoFactura.SIN_DEFINIR));
        return "admin/ordenes/detalle";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        OrdenCompraProveedor orden = ordenCompraProveedorService.buscarOrden(id);
        if (orden == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "No existe la orden de compra");
            return "redirect:/admin/ordenes";
        }
        if (orden.isEntregada()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Una orden ya recibida no se puede editar");
            return "redirect:/admin/ordenes/" + id;
        }
        model.addAttribute("proveedores", proveedorService.listarProveedorActivo());
        model.addAttribute("productos", productoService.listarProductoActivo());
        model.addAttribute("orden", orden);
        model.addAttribute("modoEdicion", true);
        return "admin/ordenes/formulario";
    }

    @PostMapping("/{id}/editar")
    public String editarOrden(@PathVariable String id,
                              @RequestParam(value = "proveedor.id", required = false) String idProveedor,
                              @RequestParam(value = "productoId", required = false) List<String> productoIds,
                              @RequestParam(value = "cantidad", required = false) List<Integer> cantidades,
                              @RequestParam(value = "precioUnitario", required = false) List<Double> precios,
                              RedirectAttributes redirectAttributes) {
        try {
            gestionOrdenProveedorService.modificarOrden(id, idProveedor, armarItems(productoIds, cantidades, precios));
            redirectAttributes.addFlashAttribute("mensajeExito", "La orden de compra fue actualizada.");
            return "redirect:/admin/ordenes/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/ordenes/" + id + "/editar";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarOrden(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            ordenCompraProveedorService.eliminarOrden(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "La orden de compra fue eliminada.");
            return "redirect:/admin/ordenes";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/ordenes/" + id;
        }
    }

    /**
     * Reemplaza al antiguo "/entregar". Marcar una orden como entregada sin más no movía el stock;
     * ahora la recepción registra la factura del proveedor (que suma el stock) y recién después
     * deja la orden entregada, todo en una sola transacción.
     */
    @PostMapping("/{id}/recibir")
    public String recibir(@PathVariable String id,
                          @RequestParam(value = "numeroFactura", required = false) Long numeroFactura,
                          @RequestParam(value = "idFormaDePago", required = false) String idFormaDePago,
                          @RequestParam(value = "estado", required = false) EstadoFactura estado,
                          RedirectAttributes redirectAttributes) {
        try {
            FacturaProveedor factura = recepcionMercaderiaService.recibirOrden(
                    id, numeroFactura != null ? numeroFactura : 0L, idFormaDePago, estado);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Mercadería recibida: se registró la factura N° " + factura.getNumeroFactura()
                            + " y se sumó el stock.");
            return "redirect:/admin/facturas-proveedor/" + factura.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/ordenes/" + id;
        }
    }

    private double calcularTotal(OrdenCompraProveedor orden) {
        double total = 0;
        for (DetalleOrdenCompraProveedor d : orden.getDetalles()) {
            total += d.getCantidad() * d.getPrecioUnitario();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    private List<ItemFacturaDTO> armarItems(List<String> productoIds, List<Integer> cantidades, List<Double> precios) {
        List<ItemFacturaDTO> items = new ArrayList<>();
        if (productoIds == null) return items;
        for (int i = 0; i < productoIds.size(); i++) {
            String pid = productoIds.get(i);
            if (pid == null || pid.isBlank()) continue;   // líneas vacías del formulario
            int qty = (cantidades != null && cantidades.size() > i && cantidades.get(i) != null) ? cantidades.get(i) : 0;
            double pu = (precios != null && precios.size() > i && precios.get(i) != null) ? precios.get(i) : 0.0;
            items.add(new ItemFacturaDTO(pid, qty, pu));
        }
        return items;
    }
}