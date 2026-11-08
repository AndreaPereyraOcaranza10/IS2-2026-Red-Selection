package redSelection.dulceMayor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import redSelection.dulceMayor.dto.CatalogoProductoDTO;
import redSelection.dulceMayor.enums.TipoMovimientoStock;
import redSelection.dulceMayor.model.Producto;
import redSelection.dulceMayor.service.CatalogoService;
import redSelection.dulceMayor.service.ProductoService;
import redSelection.dulceMayor.service.StockService;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class PortalCatalogController {
    private final CatalogoService catalogo;
    private final ProductoService productos;
    private final StockService stock;

    public PortalCatalogController(CatalogoService catalogo, ProductoService productos, StockService stock) {
        this.catalogo = catalogo;
        this.productos = productos;
        this.stock = stock;
    }

    @GetMapping("/api/products")
    public List<PortalProduct> products() {
        return catalogo.listar(null, null, false).stream().map(this::map).toList();
    }

    @GetMapping("/api/products/{id}")
    public PortalProduct product(@PathVariable String id) {
        return map(catalogo.buscar(id));
    }

    @GetMapping("/api/admin/products")
    public List<PortalProduct> inventory() {
        return catalogo.listar(null, null, true).stream().map(this::map).toList();
    }

    @PatchMapping("/api/admin/products/{id}/stock")
    @Transactional
    public PortalProduct updateStock(@PathVariable String id, @RequestBody StockUpdate update) {
        if (update.stock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El stock no puede ser negativo.");
        }
        Producto producto = productos.obtenerConBloqueo(id);
        long actual = stock.stockActual(id);
        long delta = update.stock() - actual;
        if (delta != 0) {
            if (delta > Integer.MAX_VALUE || delta < Integer.MIN_VALUE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ajuste de stock es demasiado grande.");
            }
            stock.registrar(producto, TipoMovimientoStock.AJUSTE, (int) delta, null);
        }
        return map(catalogo.buscar(id));
    }

    private PortalProduct map(CatalogoProductoDTO p) {
        String packName = p.presentacion() == null ? "Bulto" :
                (p.presentacion().name().equals("PACK") ? "Pack" : "Bulto");
        String image = p.imagenId() == null ? "" : "/api/imagenes/" + p.imagenId();
        String color = switch (p.categoria() == null ? "" : p.categoria().toLowerCase()) {
            case "alfajores" -> "var(--color-product-alfajor)";
            case "gomitas" -> "var(--color-product-gomitas)";
            case "chocolates" -> "var(--color-product-chocolate)";
            case "chicles" -> "var(--color-product-chicle)";
            case "caramelos" -> "var(--color-product-caramelo)";
            default -> "var(--color-surface-muted)";
        };
        String tag = p.enOferta() ? "Oferta mayorista" : null;
        String availability = p.stock() > 0 && p.stock() <= p.stockMinimo()
                ? "Últimos bultos" : "Disponible";
        return new PortalProduct(p.id(), p.nombre(), p.marca(), p.categoria(),
                packName + " x " + p.unidadesPorPresentacion() + " unidades",
                p.precio(), p.precioPorUnidad(), p.stock(), image, color, tag,
                p.stockMinimo(), p.disponible(), p.descripcion(), p.unidadesPorPresentacion(), availability);
    }

    public record StockUpdate(int stock) {}
    public record PortalProduct(String id, String name, String brand, String category, String pack,
                                BigDecimal price, BigDecimal unitPrice, long stock, String image, String color,
                                String tag, int stockMinimum, boolean inStock, String description,
                                int unitsPerPack, String availability) {}
}