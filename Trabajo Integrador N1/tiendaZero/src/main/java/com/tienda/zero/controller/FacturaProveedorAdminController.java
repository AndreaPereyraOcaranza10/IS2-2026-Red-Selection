package com.tienda.zero.controller;

import com.tienda.zero.model.FacturaProveedor;
import com.tienda.zero.service.FacturaProveedorService;
import com.tienda.zero.service.FacturaClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Facturas de proveedor: listado, detalle y anulación (que revierte el stock).
 * Las facturas se crean al recibir una orden de compra, no desde acá.
 */
@Controller
@RequestMapping({"/admin/facturas", "/admin/facturas-proveedor"})
public class FacturaProveedorAdminController {

    private static final String REDIRECCION_LISTA = "redirect:/admin/facturas";

    private final FacturaProveedorService facturaProveedorService;
    private final FacturaClienteService facturaClienteService;

    public FacturaProveedorAdminController(FacturaProveedorService facturaProveedorService,
                                          FacturaClienteService facturaClienteService) {
        this.facturaProveedorService = facturaProveedorService;
        this.facturaClienteService = facturaClienteService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("facturas", facturaProveedorService.listarFacturaProveedor());
        model.addAttribute("facturasClientes", facturaClienteService.listar());
        return "admin/facturas/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("factura", facturaProveedorService.buscarFacturaProveedor(id));
            return "admin/facturas/detalle";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return REDIRECCION_LISTA;
        }
    }

    @PostMapping("/{id}/cancel")
    public String anular(@PathVariable String id,
                         @RequestParam(value = "observacion", required = false) String observacion,
                         RedirectAttributes redirectAttributes) {
        try {
            FacturaProveedor factura = facturaProveedorService.anularFacturaProveedor(id, observacion);
            String mensaje = "La factura fue anulada y se revirtió el stock";
            if (factura.getOrdenCompra() != null) {
                mensaje = mensaje + ". La orden de compra volvió a quedar pendiente";
            }
            redirectAttributes.addFlashAttribute("mensajeExito", mensaje + ".");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/facturas-proveedor/" + id;
    }
}
