package com.tienda.zero.controller;

import com.tienda.zero.dto.ProductoFormDTO;
import com.tienda.zero.model.Categoria;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.SubCategoria;
import com.tienda.zero.service.CategoriaService;
import com.tienda.zero.service.GestionProductoService;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.SubCategoriaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Edición de productos y gestión de sus precios. Complementa al AdminController (alta, listado y baja).
 * Todas las rutas empiezan con /products, que SecurityConfig ya restringe a ADMINISTRATIVO y JEFE.
 */
@Controller
@RequestMapping("/products")
public class ProductoAdminController {

    private static final String VISTA_EDICION = "admin/edit-product";
    private static final String VISTA_PRECIOS = "admin/product-prices";
    private static final String REDIRECCION_INVENTARIO = "redirect:/inventory";

    private final ProductoService productoService;
    private final GestionProductoService gestionProductoService;
    private final CategoriaService categoriaService;
    private final SubCategoriaService subCategoriaService;

    public ProductoAdminController(ProductoService productoService,
                                   GestionProductoService gestionProductoService,
                                   CategoriaService categoriaService,
                                   SubCategoriaService subCategoriaService) {
        this.productoService = productoService;
        this.gestionProductoService = gestionProductoService;
        this.categoriaService = categoriaService;
        this.subCategoriaService = subCategoriaService;
    }

    @GetMapping("/{id}/edit")
    public String mostrarEdicion(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Producto producto = productoService.buscarProducto(id);
            if (producto.isEliminado()) {
                redirectAttributes.addFlashAttribute("mensajeError", "No se puede editar un producto eliminado");
                return REDIRECCION_INVENTARIO;
            }
            model.addAttribute("productForm", aFormulario(producto));
            prepararEdicion(model, producto);
            return VISTA_EDICION;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return REDIRECCION_INVENTARIO;
        }
    }

    @PostMapping("/{id}/edit")
    public String guardarEdicion(@PathVariable String id,
                                 @ModelAttribute("productForm") ProductoFormDTO form,
                                 @RequestParam(value = "image", required = false) MultipartFile image,
                                 Model model, RedirectAttributes redirectAttributes) {
        try {
            String nombreImagen = null;
            String mimeImagen = null;
            byte[] contenidoImagen = null;
            if (image != null && !image.isEmpty()) {
                nombreImagen = image.getOriginalFilename();
                mimeImagen = image.getContentType();
                contenidoImagen = image.getBytes();
            }

            Producto producto = gestionProductoService.modificarProducto(id, form.getName(), form.getDescription(),
                    form.getTalle(), form.isEnOferta(), form.getIdSubCategoria(),
                    nombreImagen, mimeImagen, contenidoImagen);

            redirectAttributes.addFlashAttribute("mensajeExito",
                    "El producto \"" + producto.getNombre() + "\" fue modificado con éxito.");
            return REDIRECCION_INVENTARIO;
        } catch (Exception e) {
            try {
                // Vuelve al formulario con lo que el usuario había escrito y el motivo del error
                Producto producto = productoService.buscarProducto(id);
                form.setId(id);
                form.setSku(producto.getCodigo());
                model.addAttribute("error", e.getMessage());
                prepararEdicion(model, producto);
                return VISTA_EDICION;
            } catch (IllegalArgumentException noExiste) {
                redirectAttributes.addFlashAttribute("mensajeError", noExiste.getMessage());
                return REDIRECCION_INVENTARIO;
            }
        }
    }

    @GetMapping("/{id}/prices")
    public String mostrarPrecios(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("producto", productoService.buscarProducto(id));
            model.addAttribute("precios", gestionProductoService.listarPrecios(id));
            return VISTA_PRECIOS;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return REDIRECCION_INVENTARIO;
        }
    }

    @PostMapping("/{id}/prices")
    public String cambiarPrecio(@PathVariable String id,
                                @RequestParam(value = "price", required = false) Double price,
                                RedirectAttributes redirectAttributes) {
        try {
            gestionProductoService.cambiarPrecio(id, price != null ? price : 0.0);
            redirectAttributes.addFlashAttribute("mensajeExito", "El precio se actualizó correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/products/" + id + "/prices";
    }

    private ProductoFormDTO aFormulario(Producto producto) {
        ProductoFormDTO form = new ProductoFormDTO();
        form.setId(producto.getId());
        form.setName(producto.getNombre());
        form.setSku(producto.getCodigo());
        form.setTalle(producto.getTalle());
        form.setEnOferta(producto.isEnOferta());
        form.setDescription(producto.getDescripcion());
        if (producto.getSubCategoria() != null) {
            form.setIdSubCategoria(producto.getSubCategoria().getId());
        }
        return form;
    }

    /** Datos que necesita la pantalla de edición además del formulario: subcategorías e imagen actual. */
    private void prepararEdicion(Model model, Producto producto) {
        model.addAttribute("subcategorias", subcategoriasActivas());
        String imagenActualUrl = null;
        if (producto.getImagen() != null && producto.getImagen().getId() != null) {
            imagenActualUrl = "/imagen/" + producto.getImagen().getId();
        }
        model.addAttribute("imagenActualUrl", imagenActualUrl);
    }

    private List<SubCategoria> subcategoriasActivas() {
        List<SubCategoria> resultado = new ArrayList<>();
        for (Categoria categoria : categoriaService.listarCategoriaActivo()) {
            resultado.addAll(subCategoriaService.listarSubCategoriaActivo(categoria.getId()));
        }
        return resultado;
    }
}
