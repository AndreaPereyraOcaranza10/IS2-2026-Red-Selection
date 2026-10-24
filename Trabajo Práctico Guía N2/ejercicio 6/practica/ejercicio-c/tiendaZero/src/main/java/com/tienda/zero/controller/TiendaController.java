package com.tienda.zero.controller;

import com.tienda.zero.dto.ProductoCardDTO;
import com.tienda.zero.enums.TipoPago;
import com.tienda.zero.model.Categoria;
import com.tienda.zero.model.Cliente;
import com.tienda.zero.model.Direccion;
import com.tienda.zero.model.Persona;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.VigenciaPrecio;
import com.tienda.zero.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;


@Controller
@RequiredArgsConstructor
public class TiendaController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final VigenciaPrecioService vigenciaPrecioService;
    private final FlujoCompraService flujoCompraService;
    private final StockService stockService;
    private final UsuarioService usuarioService;
    private final PersonaService personaService;
    private final MercadoPagoService mercadoPagoService;
    private final FacturaClienteService facturaClienteService;

    @GetMapping("/shop")
    public String shop(
            @RequestParam(value = "categoria", required = false) List<String> categoriasParam,
            @RequestParam(value = "category", required = false) List<String> categoryParam,
            @RequestParam(value = "subcategoria", required = false) List<String> subcategoriasParam,
            @RequestParam(value = "subcategory", required = false) List<String> subcategoryParam,
            @RequestParam(value = "talle", required = false) List<String> tallesParam,
            @RequestParam(value = "oferta", required = false) Boolean ofertaParam,
            @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort,
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false) Integer size,
            CsrfToken csrfToken,
            Model model
    ) {
        csrfToken.getToken();

        try {
            // Normalizar filtros recibidos por URL.
            List<String> tallesFiltro = (tallesParam != null)
                    ? tallesParam.stream().filter(t -> t != null && !t.isBlank()).map(String::trim).distinct().collect(Collectors.toList())
                    : java.util.Collections.emptyList();

            List<Categoria> categorias = categoriaService.listarCategoriaActivo();
            List<Categoria> categoriasDisponibles = categorias != null ? categorias : java.util.Collections.emptyList();

            List<Producto> productosActivos = productoService.listarProductoActivo();
            List<ProductoCardDTO> allCards = (productosActivos != null)
                    ? productosActivos.stream().map(this::convertirAProductoCardDTO).collect(Collectors.toList())
                    : new java.util.ArrayList<>();

            List<String> subcategoriasDisponibles = allCards.stream()
                    .map(ProductoCardDTO::getSubcategory)
                    .filter(s -> s != null && !s.isBlank())
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());

            java.util.Set<String> nombresCategorias = categoriasDisponibles.stream()
                    .map(Categoria::getNombre)
                    .filter(n -> n != null && !n.isBlank())
                    .map(n -> n.trim().toLowerCase())
                    .collect(Collectors.toSet());
            java.util.Set<String> slugsCategorias = categoriasDisponibles.stream()
                    .map(Categoria::getNombre)
                    .filter(n -> n != null && !n.isBlank())
                    .map(n -> n.trim().toLowerCase().replace(" ", "-"))
                    .collect(Collectors.toSet());
            java.util.Set<String> nombresSubcategorias = subcategoriasDisponibles.stream()
                    .map(s -> s.trim().toLowerCase())
                    .collect(Collectors.toSet());

            List<String> categoriasFiltro = new java.util.ArrayList<>();
            List<String> subcategoriasFiltro = new java.util.ArrayList<>();

            List<String> categoriasRecibidas = new java.util.ArrayList<>();
            if (categoriasParam != null) categoriasRecibidas.addAll(categoriasParam);
            if (categoryParam != null) categoriasRecibidas.addAll(categoryParam);
            for (String valor : limpiarValores(categoriasRecibidas)) {
                String normalizado = valor.toLowerCase();
                if (nombresCategorias.contains(normalizado) || slugsCategorias.contains(normalizado)) {
                    categoriasFiltro.add(valor);
                } else if (nombresSubcategorias.contains(normalizado)) {
                    subcategoriasFiltro.add(valor);
                }
            }

            List<String> subcategoriasRecibidas = new java.util.ArrayList<>();
            if (subcategoriasParam != null) subcategoriasRecibidas.addAll(subcategoriasParam);
            if (subcategoryParam != null) subcategoriasRecibidas.addAll(subcategoryParam);
            subcategoriasFiltro.addAll(limpiarValores(subcategoriasRecibidas));

            categoriasFiltro = limpiarValores(categoriasFiltro);
            subcategoriasFiltro = limpiarValores(subcategoriasFiltro);

            // 1. Filtrar por búsqueda q
            if (query != null && !query.isBlank()) {
                String qLower = query.trim().toLowerCase();
                allCards = allCards.stream()
                        .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(qLower))
                                || (p.getDescription() != null && p.getDescription().toLowerCase().contains(qLower))
                                || (p.getCategory() != null && p.getCategory().toLowerCase().contains(qLower))
                                || (p.getSku() != null && p.getSku().toLowerCase().contains(qLower)))
                        .collect(Collectors.toList());
            }

            // 2. Filtrar por categorías seleccionadas
            if (!categoriasFiltro.isEmpty()) {
                List<String> finalCats = categoriasFiltro;
                allCards = allCards.stream()
                        .filter(p -> finalCats.stream().anyMatch(cf ->
                                cf.equalsIgnoreCase(p.getCategory())
                                || cf.equalsIgnoreCase(p.getCategorySlug())
                        ))
                        .collect(Collectors.toList());
            }

            // 3. Filtrar por subcategorias seleccionadas
            if (!subcategoriasFiltro.isEmpty()) {
                List<String> finalSubcats = subcategoriasFiltro;
                allCards = allCards.stream()
                        .filter(p -> p.getSubcategory() != null
                                && finalSubcats.stream().anyMatch(sf -> sf.equalsIgnoreCase(p.getSubcategory())))
                        .collect(Collectors.toList());
            }

            // 4. Filtrar por talles seleccionados
            if (!tallesFiltro.isEmpty()) {
                List<String> finalTalles = tallesFiltro;
                allCards = allCards.stream()
                        .filter(p -> finalTalles.stream().anyMatch(tf -> tf.equalsIgnoreCase(p.getTalle())))
                        .collect(Collectors.toList());
            }

            // 5. Filtrar por oferta
            if (Boolean.TRUE.equals(ofertaParam)) {
                allCards = allCards.stream()
                        .filter(p -> p.getOldPrice() != null)
                        .collect(Collectors.toList());
            }

            // 6. Ordenamiento
            switch (sort != null ? sort : "latest") {
                case "price_asc" -> allCards.sort(java.util.Comparator.comparingDouble(ProductoCardDTO::getPrice));
                case "price_desc" -> allCards.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                case "az" -> allCards.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName() != null ? a.getName() : "", b.getName() != null ? b.getName() : ""));
                case "za" -> allCards.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(b.getName() != null ? b.getName() : "", a.getName() != null ? a.getName() : ""));
                case "popularity" -> allCards.sort((a, b) -> Integer.compare(b.getReviewCount(), a.getReviewCount()));
                default -> {} // orden natural / más recientes
            }

            // 7. Paginación
            int totalItems = allCards.size();
            int pageSize = size != null && size > 0 ? size : Math.max(totalItems, 1);
            int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
            int validPage = Math.max(1, Math.min(page, totalPages));
            int fromIndex = (validPage - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, totalItems);

            List<ProductoCardDTO> pageProducts = (fromIndex < totalItems)
                    ? allCards.subList(fromIndex, toIndex)
                    : java.util.Collections.emptyList();

            model.addAttribute("products", pageProducts);
            model.addAttribute("currentPage", validPage);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalItems", totalItems);
            model.addAttribute("selectedCategorias", categoriasFiltro);
            model.addAttribute("selectedSubcategorias", subcategoriasFiltro);
            model.addAttribute("selectedTalles", tallesFiltro);
            model.addAttribute("currentOferta", ofertaParam);
            model.addAttribute("currentSort", sort != null ? sort : "latest");
            model.addAttribute("query", query != null ? query.trim() : null);

            boolean hasActiveFilters = !categoriasFiltro.isEmpty()
                    || !subcategoriasFiltro.isEmpty()
                    || !tallesFiltro.isEmpty()
                    || Boolean.TRUE.equals(ofertaParam)
                    || (query != null && !query.isBlank());
            model.addAttribute("hasActiveFilters", hasActiveFilters);

            // Categorías y talles disponibles para los filtros
            model.addAttribute("categories", categoriasDisponibles);
            model.addAttribute("subcategories", subcategoriasDisponibles);

            List<String> tallesDisponibles = java.util.List.of("S", "M", "L", "XL", "XXL", "38", "39", "40", "41", "42", "Único");
            model.addAttribute("tallesDisponibles", tallesDisponibles);

        } catch (Exception e) {
            System.err.println("Advertencia al cargar catálogo: " + e.getMessage());
            e.printStackTrace();
        }

        return "tienda/shop";
    }

    private List<String> limpiarValores(List<String> valores) {
        if (valores == null) {
            return java.util.Collections.emptyList();
        }
        return valores.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    @GetMapping("/tienda/envios")
    public String envios() { return "tienda/envios"; }

    @GetMapping({"/product", "/product/{id}"})
    public String singleProduct(@PathVariable(required = false) String id, CsrfToken csrfToken, Model model) {
        csrfToken.getToken();

        try {
            if (id != null) {
                Producto prod = productoService.buscarProducto(id);
                if (prod != null) {
                    model.addAttribute("product", convertirAProductoCardDTO(prod));
                }
            }

            List<Producto> productosActivos = productoService.listarProductoActivo();
            if (productosActivos != null && !productosActivos.isEmpty()) {
                List<ProductoCardDTO> related = productosActivos.stream()
                        .map(this::convertirAProductoCardDTO)
                        .filter(producto -> id == null || !id.equals(producto.getId()))
                        .limit(4)
                        .collect(Collectors.toList());
                model.addAttribute("relatedProducts", related);
            }
        } catch (Exception e) {
            System.err.println("Advertencia al cargar producto: " + e.getMessage());
        }

        return "tienda/single-product-page";
    }

    @GetMapping("/cart")
    public String cart(Authentication auth, Model model) {
        var carrito = flujoCompraService.verCarrito(auth.getName());
        model.addAttribute("cartItems", carrito.get("items"));
        model.addAttribute("cartSubtotal", carrito.get("total"));
        model.addAttribute("cartTaxes", 0);
        model.addAttribute("cartShipping", 0);
        model.addAttribute("cartTotal", carrito.get("total"));
        return "tienda/cart";
    }

    @GetMapping("/checkout")
    public String checkout(Authentication auth, Model model) {
        model.addAttribute("cart", flujoCompraService.verCarrito(auth.getName()));
        boolean esCliente = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENTE"));
        model.addAttribute("formasPago", esCliente ? List.of(TipoPago.BILLETERA_VIRTUAL) : List.of(TipoPago.values()));
        model.addAttribute("checkoutCliente", esCliente);
        model.addAttribute("direccionGuardada", direccionGuardada(auth.getName()));
        return "tienda/checkout";
    }

    private String direccionGuardada(String username) {
        var usuario = usuarioService.buscarUsuarioPorNombreUsuario(username);
        var persona = personaService.buscarPersonaPorUsuario(usuario.getId());
        if (persona.isEmpty()) return "";

        Persona titular = persona.get();
        String direccion = titular.getDirecciones().stream()
                .filter(d -> !d.isEliminado())
                .findFirst()
                .map(this::formatearDireccion)
                .orElse("");
        if (!direccion.isBlank()) return direccion;
        if (titular instanceof Cliente cliente && cliente.getDireccionEstadia() != null) {
            return limitarDireccion(cliente.getDireccionEstadia().trim());
        }
        return "";
    }

    private String formatearDireccion(Direccion direccion) {
        List<String> partes = new ArrayList<>();
        agregarParte(partes, direccion.getCalle());
        agregarParte(partes, direccion.getNumeracion());
        agregarParte(partes, direccion.getBarrio());
        agregarParte(partes, direccion.getManzanaPiso());
        agregarParte(partes, direccion.getCasaDepartamento());
        if (direccion.getLocalidad() != null) {
            agregarParte(partes, direccion.getLocalidad().getNombre());
            agregarParte(partes, direccion.getLocalidad().getCodigoPostal());
            if (direccion.getLocalidad().getDepartamento() != null) {
                agregarParte(partes, direccion.getLocalidad().getDepartamento().getNombre());
                if (direccion.getLocalidad().getDepartamento().getProvincia() != null) {
                    agregarParte(partes, direccion.getLocalidad().getDepartamento().getProvincia().getNombre());
                }
            }
        }
        agregarParte(partes, direccion.getReferencia());
        return limitarDireccion(String.join(", ", partes));
    }

    private String limitarDireccion(String direccion) {
        return direccion.length() > 250 ? direccion.substring(0, 250) : direccion;
    }

    private void agregarParte(List<String> partes, String parte) {
        if (parte != null && !parte.isBlank()) partes.add(parte.trim());
    }

    @PostMapping("/cart/add")
    public String agregarAlCarrito(@RequestParam String productId, @RequestParam(defaultValue = "1") int quantity, Authentication auth, RedirectAttributes flash) {
        try { flujoCompraService.agregarAlCarrito(auth.getName(), productId, quantity); flash.addFlashAttribute("mensajeExito", "Producto agregado al carrito."); }
        catch (RuntimeException e) { flash.addFlashAttribute("mensajeError", e.getMessage()); }
        return "redirect:/cart";
    }

    @PostMapping(value = "/cart/add", headers = "X-Requested-With=XMLHttpRequest")
    public ResponseEntity<Map<String, Object>> agregarAlCarritoAjax(
            @RequestParam String productId,
            @RequestParam(defaultValue = "1") int quantity,
            Authentication auth) {
        try {
            flujoCompraService.agregarAlCarrito(auth.getName(), productId, quantity);
            return ResponseEntity.ok(Map.of("success", true, "message", "Producto agregado al carrito."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/cart/remove/{productId}")
    public String quitarDelCarrito(@PathVariable String productId, Authentication auth, RedirectAttributes flash) {
        try {
            flujoCompraService.quitarDelCarrito(auth.getName(), productId);
        } catch (RuntimeException e) {
            flash.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/update/{productId}")
    public String actualizarCantidadCarrito(@PathVariable String productId, @RequestParam int quantity, Authentication auth, RedirectAttributes flash) {
        try { flujoCompraService.actualizarCantidadCarrito(auth.getName(), productId, quantity); }
        catch (RuntimeException e) { flash.addFlashAttribute("mensajeError", e.getMessage()); }
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String vaciarCarrito(Authentication auth, RedirectAttributes flash) {
        try {
            flujoCompraService.vaciarCarrito(auth.getName());
            flash.addFlashAttribute("mensajeExito", "Se vació el carrito.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String confirmarCompra(@RequestParam String address,
                                  @RequestParam TipoPago formaPago,
                                  Authentication auth,
                                  RedirectAttributes flash) {
        try {
            var pedido = flujoCompraService.crearOrdenCliente(auth.getName(), address, formaPago);
            if (facturaClienteService.requiereFormulario(pedido)) {
                return "redirect:/admin/facturas/clientes/orden/" + pedido.getId() + "/nueva";
            }
            if (formaPago == TipoPago.BILLETERA_VIRTUAL) {
                try {
                    String checkoutUrl = mercadoPagoService.crearCheckout(pedido);
                    return "redirect:" + checkoutUrl;
                } catch (RuntimeException e) {
                    try {
                        flujoCompraService.anularOrdenCliente(pedido.getId(), auth.getName());
                    } catch (RuntimeException ignored) {
                        // La orden queda disponible para revisión si no se puede compensar.
                    }
                    throw e;
                }
            }
            flash.addFlashAttribute("mensajeExito", "Orden creada. Podés seguir su estado desde esta sección. Número: " + pedido.getIdentificadorCompra());
            return "redirect:/orders";
        } catch (RuntimeException e) {
            flash.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/orders")
    public String orders(Authentication auth, Model model) {
        // La consulta de pedidos debe seguir disponible aunque Mercado Pago no responda.
        try {
            mercadoPagoService.sincronizarPagosPendientesUsuario(auth.getName());
        } catch (RuntimeException e) {
            // La sincronización se reintentará en la próxima visita; no bloquear el historial.
        }
        model.addAttribute("orders", flujoCompraService.listarPedidosUsuario(auth.getName()));
        model.addAttribute("facturasPedidos", facturaClienteService.delUsuario(auth.getName()));
        return "tienda/orders";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelarPedido(@PathVariable String id, Authentication auth, RedirectAttributes flash) {
        try { flujoCompraService.anularOrdenCliente(id, auth.getName()); flash.addFlashAttribute("mensajeExito", "La orden fue anulada y el stock reservado fue reintegrado."); }
        catch (RuntimeException e) { flash.addFlashAttribute("mensajeError", e.getMessage()); }
        return "redirect:/orders";
    }

    private ProductoCardDTO convertirAProductoCardDTO(Producto prod) {
        double precio = 0.0;
        try {
            VigenciaPrecio vigencia = vigenciaPrecioService.buscarVigenciaPrecioVigente(prod.getId());
            if (vigencia != null) {
                precio = vigencia.getPrecio();
            }
        } catch (Exception ignored) {
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
                        : null)
                .sku(prod.getCodigo())
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
