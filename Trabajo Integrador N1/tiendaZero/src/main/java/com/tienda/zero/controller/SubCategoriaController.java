package com.tienda.zero.controller;

import com.tienda.zero.model.SubCategoria;
import com.tienda.zero.service.CategoriaService;
import com.tienda.zero.service.SubCategoriaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Altas, modificaciones y bajas de subcategorías. Las rutas cuelgan de su categoría,
 * y todas vuelven al detalle de esa categoría.
 */
@Controller
@RequestMapping("/admin/categorias/{idCategoria}/subcategorias")
public class SubCategoriaController {

    private final SubCategoriaService subCategoriaService;
    private final CategoriaService categoriaService;

    public SubCategoriaController(SubCategoriaService subCategoriaService, CategoriaService categoriaService) {
        this.subCategoriaService = subCategoriaService;
        this.categoriaService = categoriaService;
    }

    @PostMapping("/nueva")
    public String crear(@PathVariable String idCategoria, @RequestParam(required = false) String nombre,
                        RedirectAttributes redirectAttributes) {
        try {
            subCategoriaService.crearSubCategoria(nombre, idCategoria);
            redirectAttributes.addFlashAttribute("exito", "Subcategoría agregada correctamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("nombreNueva", nombre);
        }
        return "redirect:/admin/categorias/" + idCategoria;
    }

    @GetMapping("/{id}/editar")
    public String mostrarModificacion(@PathVariable String idCategoria, @PathVariable String id,
                                      Model model, RedirectAttributes redirectAttributes) {
        try {
            SubCategoria subCategoria = subCategoriaService.buscarSubCategoria(id);
            prepararFormulario(model, idCategoria, id, subCategoria.getNombre());
            return "admin/categorias/subcategoria-formulario";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/categorias/" + idCategoria;
        }
    }

    @PostMapping("/{id}/editar")
    public String modificar(@PathVariable String idCategoria, @PathVariable String id,
                            @RequestParam(required = false) String nombre,
                            Model model, RedirectAttributes redirectAttributes) {
        try {
            subCategoriaService.modificarSubCategoria(id, nombre, idCategoria);
            redirectAttributes.addFlashAttribute("exito", "Subcategoría modificada correctamente");
            return "redirect:/admin/categorias/" + idCategoria;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            prepararFormulario(model, idCategoria, id, nombre);
            return "admin/categorias/subcategoria-formulario";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String idCategoria, @PathVariable String id,
                           RedirectAttributes redirectAttributes) {
        try {
            subCategoriaService.eliminarSubCategoria(id);
            redirectAttributes.addFlashAttribute("exito", "Subcategoría eliminada correctamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categorias/" + idCategoria;
    }

    private void prepararFormulario(Model model, String idCategoria, String id, String nombre) {
        model.addAttribute("categoria", categoriaService.buscarCategoria(idCategoria));
        model.addAttribute("accion", "/admin/categorias/" + idCategoria + "/subcategorias/" + id + "/editar");
        model.addAttribute("nombre", nombre);
    }
}