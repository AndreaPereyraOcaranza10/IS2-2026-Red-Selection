package com.tienda.zero.controller;

import com.tienda.zero.model.Proveedor;
import com.tienda.zero.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    public String lista(Model model) {
        List<Proveedor> proveedores = proveedorService.listarProveedorActivo();
        model.addAttribute("proveedores", proveedores);
        return "admin/proveedores/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "admin/proveedores/formulario";
    }

    @PostMapping("/nuevo")
    public String crear(@ModelAttribute Proveedor proveedor) {
        proveedorService.crearProveedor(proveedor.getRazonSocial(), null);
        return "redirect:/admin/proveedores";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model) {
        Proveedor p = proveedorService.buscarProveedor(id);
        model.addAttribute("proveedor", p);
        return "admin/proveedores/formulario";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable String id, @ModelAttribute Proveedor proveedor) {
        proveedorService.modificarProveedor(id, proveedor.getRazonSocial(), null);
        return "redirect:/admin/proveedores";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id) {
        proveedorService.eliminarProveedor(id);
        return "redirect:/admin/proveedores";
    }
}
