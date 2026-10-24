package com.tienda.zero.controller;

import com.tienda.zero.dto.ProductoCardDTO;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.VigenciaPrecio;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.VigenciaPrecioService;
import com.tienda.zero.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductoService productoService;
    private final VigenciaPrecioService vigenciaPrecioService;
    private final StockService stockService;

    @GetMapping({"/", "/index", "/index.html"})
    public String index(CsrfToken csrfToken, Model model) {
        csrfToken.getToken();

        try {
            List<Producto> productosActivos = productoService.listarProductoActivo();

            if (productosActivos != null && !productosActivos.isEmpty()) {
                List<ProductoCardDTO> productCards = productosActivos.stream()
                        .map(this::convertirAProductoCardDTO)
                        .collect(Collectors.toList());

                // Se envían al modelo sólo si hay productos, permitiendo que el template muestre el fallback si la BD está vacía
                model.addAttribute("popularProducts", productCards.stream().limit(4).toList());
                model.addAttribute("latestProducts", productCards.stream().skip(Math.max(0, productCards.size() - 4)).toList());
            }
        } catch (Exception e) {
            // Log de advertencia y continuamos para permitir renderizado de la portada
            System.err.println("Advertencia al cargar productos para la home: " + e.getMessage());
        }

        return "tienda/index";
    }

    private ProductoCardDTO convertirAProductoCardDTO(Producto prod) {
        double precio = 0.0;
        try {
            VigenciaPrecio vigencia = vigenciaPrecioService.buscarVigenciaPrecioVigente(prod.getId());
            if (vigencia != null) {
                precio = vigencia.getPrecio();
            }
        } catch (Exception e) {
            // Si aún no tiene vigencia cargada, se mantiene precio en 0.0
        }

        String imagenUrl = (prod.getImagen() != null && prod.getImagen().getId() != null)
                ? "/imagen/" + prod.getImagen().getId()
                : "/assets/images/products/1.jpg";

        String categoria = "General";
        if (prod.getSubCategoria() != null) {
            if (prod.getSubCategoria().getCategoria() != null) {
                categoria = prod.getSubCategoria().getCategoria().getNombre();
            } else {
                categoria = prod.getSubCategoria().getNombre();
            }
        }

        return ProductoCardDTO.builder()
                .id(prod.getId())
                .name(prod.getNombre())
                .category(categoria)
                .categorySlug(categoria.toLowerCase().replace(" ", "-"))
                .subcategory(prod.getSubCategoria() != null ? prod.getSubCategoria().getNombre() : null)
                .description(prod.getDescripcion() != null && !prod.getDescripcion().isBlank() 
                        ? prod.getDescripcion() 
                        : "Indumentaria deportiva oficial Zero.")
                .sku(prod.getCodigo() != null ? prod.getCodigo() : "ZERO-001")
                .brand("Zero")
                .talle(prod.getTalle() != null ? prod.getTalle() : "-")
                .inStock(stockService.calcularStockActual(prod.getId()) > 0)
                .reviewCount(5)
                .rating(5)
                .price(prod.isEnOferta() ? Math.round(precio * (1 - prod.getPorcentajeDescuento() / 100.0) * 100.0) / 100.0 : precio)
                .oldPrice(prod.isEnOferta() ? precio : null)
                .imageUrl(imagenUrl)
                .build();
    }
}
