package com.tienda.zero.controller;

import com.tienda.zero.model.Categoria;
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
 * ABM de categorías. El detalle de cada categoría muestra y gestiona sus subcategorías
 * (las altas, modificaciones y bajas de subcategorías las atiende SubCategoriaController).
 */
@Controller
@RequestMapping("/admin/categorias")
public class CategoriaController {

    private static final String VISTA_LISTA = "admin/categorias/lista";
    private static final String VISTA_FORMULARIO = "admin/categorias/formulario";
    private static final String VISTA_DETALLE = "admin/categorias/detalle";
    private static final String REDIRECCION_LISTA = "redirect:/admin/categorias";

    private final CategoriaService categoriaService;
    private final SubCategoriaService subCategoriaService;

    public CategoriaController(CategoriaService categoriaService, SubCategoriaService subCategoriaService) {
        this.categoriaService = categoriaService;
        this.subCategoriaService = subCategoriaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listarCategoria());
        return VISTA_LISTA;
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = categoriaService.buscarCategoria(id);
            model.addAttribute("categoria", categoria);
            model.addAttribute("subcategorias", subCategoriaService.listarSubCategoria(id));
            return VISTA_DETALLE;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return REDIRECCION_LISTA;
        }
    }

    @GetMapping("/nueva")
    public String mostrarAlta(Model model) {
        prepararFormulario(model, "Nueva categoría", "/admin/categorias/nueva", null);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/nueva")
    public String crear(@RequestParam(required = false) String nombre,
                        Model model, RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = categoriaService.crearCategoria(nombre);
            redirectAttributes.addFlashAttribute("exito", "Categoría creada. Ahora podés agregarle subcategorías.");
            return "redirect:/admin/categorias/" + categoria.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            prepararFormulario(model, "Nueva categoría", "/admin/categorias/nueva", nombre);
            return VISTA_FORMULARIO;
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarModificacion(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = categoriaService.buscarCategoria(id);
            prepararFormulario(model, "Modificar categoría", "/admin/categorias/" + id + "/editar", categoria.getNombre());
            return VISTA_FORMULARIO;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return REDIRECCION_LISTA;
        }
    }

    @PostMapping("/{id}/editar")
    public String modificar(@PathVariable String id, @RequestParam(required = false) String nombre,
                            Model model, RedirectAttributes redirectAttributes) {
        try {
            categoriaService.modificarCategoria(id, nombre);
            redirectAttributes.addFlashAttribute("exito", "Categoría modificada correctamente");
            return REDIRECCION_LISTA;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            prepararFormulario(model, "Modificar categoría", "/admin/categorias/" + id + "/editar", nombre);
            return VISTA_FORMULARIO;
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            categoriaService.eliminarCategoria(id);
            redirectAttributes.addFlashAttribute("exito", "Categoría eliminada correctamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECCION_LISTA;
    }

    private void prepararFormulario(Model model, String titulo, String accion, String nombre) {
        model.addAttribute("titulo", titulo);
        model.addAttribute("accion", accion);
        model.addAttribute("nombre", nombre);
    }
}