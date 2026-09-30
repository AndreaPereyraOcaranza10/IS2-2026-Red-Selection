package com.tienda.zero.controller;

import com.tienda.zero.dto.ProductoFormDTO;
import com.tienda.zero.dto.ProductoInventarioDTO;
import com.tienda.zero.enums.TipoImagen;
import com.tienda.zero.model.Imagen;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.VigenciaPrecio;
import com.tienda.zero.repository.SubCategoriaRepository;
import com.tienda.zero.service.StockService;
import com.tienda.zero.service.ImagenService;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.VigenciaPrecioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final ProductoService productoService;
    private final VigenciaPrecioService vigenciaPrecioService;
    private final SubCategoriaRepository subCategoriaRepository;
    private final ImagenService imagenService;
    private final StockService stockService;

    @GetMapping({"/admin", "/admin/dashboard", "/admin/index"})
    public String dashboard() {
        return "admin/index";
    }

    @GetMapping({"/inventory", "/admin/inventory"})
    public String inventory(@RequestParam(value = "q", required = false) String query, Model model) {
        List<Producto> productos = productoService.listarProductoActivo();

        if (query != null && !query.isBlank()) {
            String q = query.trim().toLowerCase();
            productos = productos.stream()
                    .filter(p -> (p.getNombre() != null && p.getNombre().toLowerCase().contains(q))
                            || (p.getCodigo() != null && p.getCodigo().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
            model.addAttribute("query", query);
        }

        List<ProductoInventarioDTO> productosDTO = productos.stream().map(p -> {
            double precio = 0.0;
            try {
                VigenciaPrecio vig = vigenciaPrecioService.buscarVigenciaPrecioVigente(p.getId());
                if (vig != null) {
                    precio = vig.getPrecio();
                }
            } catch (Exception ignored) {
            }

            String imagenUrl = (p.getImagen() != null && p.getImagen().getId() != null)
                    ? "/imagen/" + p.getImagen().getId()
                    : "/assets/images/products/1.jpg";

            String categoria = "Sin categoría";
            if (p.getSubCategoria() != null) {
                if (p.getSubCategoria().getCategoria() != null) {
                    categoria = p.getSubCategoria().getCategoria().getNombre() + " - " + p.getSubCategoria().getNombre();
                } else {
                    categoria = p.getSubCategoria().getNombre();
                }
            }

            return ProductoInventarioDTO.builder()
                    .id(p.getId())
                    .name(p.getNombre())
                    .code(p.getCodigo())
                    .category(categoria)
                    .talle(p.getTalle() != null ? p.getTalle() : "-")
                    .price(precio)
                    .priceText(String.format(Locale.US, "$%.2f", precio))
                    .stock(stockService.calcularStockActual(p.getId()))
                    .enOferta(p.isEnOferta())
                    .imageUrl(imagenUrl)
                    .build();
        }).collect(Collectors.toList());

        model.addAttribute("products", productosDTO);
        return "admin/inventory";
    }

    @GetMapping({"/products/new", "/admin/products/new"})
    public String createProduct(Model model) {
        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", new ProductoFormDTO());
        }
        model.addAttribute("subcategorias", subCategoriaRepository.findByEliminadoFalse());
        return "admin/create-product";
    }

    @PostMapping("/products")
    public String saveProduct(@ModelAttribute("productForm") ProductoFormDTO form,
                              @RequestParam(value = "image", required = false) MultipartFile image,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        try {
            String idImagen = null;
            if (image != null && !image.isEmpty()) {
                Imagen imagenGuardada = imagenService.crearImagen(
                        image.getOriginalFilename(),
                        image.getContentType(),
                        image.getBytes(),
                        TipoImagen.PRODUCTO
                );
                idImagen = imagenGuardada.getId();
            }

            Producto producto = productoService.crearProducto(
                    form.getSku(),
                    form.getName(),
                    form.getDescription(),
                    form.getTalle(),
                    form.isEnOferta(),
                    idImagen,
                    form.getIdSubCategoria()
            );

            double precio = (form.getPrice() != null) ? form.getPrice() : 0.0;
            if (precio > 0) {
                vigenciaPrecioService.crearVigenciaPrecio(
                        new Date(System.currentTimeMillis()),
                        null,
                        precio,
                        producto.getId()
                );
            }

            redirectAttributes.addFlashAttribute("mensajeExito",
                    "El producto \"" + producto.getNombre() + "\" fue creado con éxito.");
            return "redirect:/inventory";

        } catch (Exception e) {
            model.addAttribute("error", "Error al crear producto: " + e.getMessage());
            model.addAttribute("productForm", form);
            model.addAttribute("subcategorias", subCategoriaRepository.findByEliminadoFalse());
            return "admin/create-product";
        }
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Producto eliminado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar producto: " + e.getMessage());
        }
        return "redirect:/inventory";
    }

    @GetMapping({"/reports", "/admin/reports"})
    public String reports() {
        return "admin/reports";
    }

    @GetMapping({"/docs", "/admin/docs"})
    public String docs() {
        return "admin/docs";
    }

    @GetMapping("/admin/404")
    public String error404() {
        return "admin/404-error";
    }
}

