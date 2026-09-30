package com.tienda.zero.controller;

import com.tienda.zero.dto.ItemFacturaDTO;
import com.tienda.zero.enums.EstadoFactura;
import com.tienda.zero.model.DetalleOrdenCompraProveedor;
import com.tienda.zero.model.FacturaProveedor;
import com.tienda.zero.model.OrdenCompraProveedor;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.Proveedor;
import com.tienda.zero.service.FormaDePagoService;
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

    @GetMapping
    public String lista(Model model) {
        List<OrdenCompraProveedor> ordenes = ordenCompraProveedorService.listarOrdenes();
        model.addAttribute("ordenes", ordenes);
        return "admin/ordenes/lista";
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
     * El alta ahora pasa por GestionOrdenProveedorService, que valida antes de guardar:
     * al menos una línea, cantidades y precios positivos, sin productos repetidos y sin
     * proveedor ni productos eliminados. Si algo falla, vuelve al formulario con el motivo.
     */
    @PostMapping("/nueva")
    public String crearOrden(@ModelAttribute OrdenCompraProveedor ordenCompraProveedor,
                             @RequestParam(value = "productoId", required = false) List<String> productoIds,
                             @RequestParam(value = "cantidad", required = false) List<Integer> cantidades,
                             @RequestParam(value = "precioUnitario", required = false) List<Double> precios,
                             RedirectAttributes redirectAttributes) {
        List<ItemFacturaDTO> items = new ArrayList<>();
        if (productoIds != null) {
            for (int i = 0; i < productoIds.size(); i++) {
                String pid = productoIds.get(i);
                if (pid == null || pid.isBlank()) continue;   // líneas vacías del formulario
                int qty = (cantidades != null && cantidades.size() > i && cantidades.get(i) != null) ? cantidades.get(i) : 0;
                double pu = (precios != null && precios.size() > i && precios.get(i) != null) ? precios.get(i) : 0.0;
                items.add(new ItemFacturaDTO(pid, qty, pu));
            }
        }

        String idProveedor = ordenCompraProveedor.getProveedor() != null
                ? ordenCompraProveedor.getProveedor().getId() : null;
        try {
            gestionOrdenProveedorService.crearOrden(idProveedor, items);
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
}