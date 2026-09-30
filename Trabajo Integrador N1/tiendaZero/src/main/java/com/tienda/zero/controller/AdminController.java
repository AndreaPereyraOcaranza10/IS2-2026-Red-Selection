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
import com.tienda.zero.service.ReporteVentasService;
import com.tienda.zero.service.VigenciaPrecioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Date;
import java.time.LocalDate;
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
    private final ReporteVentasService reporteVentasService;

    @GetMapping({"/admin", "/admin/dashboard", "/admin/index"})
    public String dashboard() {
        return "admin/index";
    }

    @GetMapping({"/inventory", "/admin/inventory"})
    public String inventory(@RequestParam(value = "q", required = false) String query,
                            @RequestParam(value = "page", defaultValue = "0") int requestedPage,
                            Model model) {
        List<Producto> productos = productoService.listarProductoActivo();

        if (query != null && !query.isBlank()) {
            String q = query.trim().toLowerCase();
            productos = productos.stream()
                    .filter(p -> (p.getNombre() != null && p.getNombre().toLowerCase().contains(q))
                            || (p.getCodigo() != null && p.getCodigo().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
            model.addAttribute("query", query);
        }

        int pageSize = 10;
        int totalPages = (int) Math.ceil(productos.size() / (double) pageSize);
        int page = Math.max(0, Math.min(requestedPage, Math.max(0, totalPages - 1)));
        int fromIndex = Math.min(page * pageSize, productos.size());
        List<Producto> paginaProductos = productos.subList(fromIndex, Math.min(fromIndex + pageSize, productos.size()));

        List<ProductoInventarioDTO> productosDTO = paginaProductos.stream().map(p -> {
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
                    .precioOferta(p.isEnOferta() ? precio * (1 - p.getPorcentajeDescuento() / 100.0) : precio)
                    .descuento(p.getPorcentajeDescuento())
                    .stock(stockService.calcularStockActual(p.getId()))
                    .enOferta(p.isEnOferta())
                    .imageUrl(imagenUrl)
                    .build();
        }).collect(Collectors.toList());

        model.addAttribute("products", productosDTO);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalProducts", productos.size());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("startProduct", productos.isEmpty() ? 0 : fromIndex + 1);
        model.addAttribute("endProduct", Math.min(fromIndex + pageSize, productos.size()));
        model.addAttribute("query", query);
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
                    false,
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

    @PostMapping("/products/inflation")
    public String actualizarPorInflacion(@RequestParam double porcentaje, RedirectAttributes redirectAttributes) {
        try {
            int cantidad = vigenciaPrecioService.actualizarPreciosPorInflacion(porcentaje);
            redirectAttributes.addFlashAttribute("mensajeExito", "Precios actualizados por inflación del " + porcentaje + "% en " + cantidad + " productos.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudieron actualizar los precios: " + e.getMessage());
        }
        return "redirect:/inventory";
    }

    @PostMapping("/products/{id}/offer")
    public String toggleOffer(@PathVariable String id, @RequestParam double porcentaje,
                              RedirectAttributes redirectAttributes) {
        try {
            Producto producto = productoService.actualizarOferta(id, porcentaje);
            redirectAttributes.addFlashAttribute("mensajeExito", porcentaje == 0
                    ? "Se quitó la oferta de " + producto.getNombre() + "."
                    : producto.getNombre() + " quedó en oferta con " + porcentaje + "% de descuento.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo cambiar la oferta: " + e.getMessage());
        }
        return "redirect:/inventory";
    }

    @PostMapping("/products/{id}/offer/remove")
    public String removeOffer(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Producto producto = productoService.actualizarOferta(id, 0);
            redirectAttributes.addFlashAttribute("mensajeExito", "Se quitó la oferta de " + producto.getNombre() + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo quitar la oferta: " + e.getMessage());
        }
        return "redirect:/inventory";
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
    public String reports(@RequestParam(value = "desde", required = false) LocalDate desde,
                          @RequestParam(value = "hasta", required = false) LocalDate hasta,
                          Model model) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaDesde = desde != null ? desde : hoy.withDayOfMonth(1);
        LocalDate fechaHasta = hasta != null ? hasta : hoy;
        if (fechaHasta.isBefore(fechaDesde)) {
            fechaHasta = fechaDesde;
        }
        model.addAttribute("reporteVentas", reporteVentasService.generar(fechaDesde, fechaHasta));
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

