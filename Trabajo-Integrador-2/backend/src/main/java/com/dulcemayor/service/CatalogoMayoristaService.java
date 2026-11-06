package com.dulcemayor.service;

import com.dulcemayor.dto.ProductResponseDTO;
import com.dulcemayor.dto.ProductWriteDTO;
import com.dulcemayor.model.Categoria;
import com.dulcemayor.model.Producto;
import com.dulcemayor.model.SubCategoria;
import com.dulcemayor.model.VigenciaPrecio;
import com.dulcemayor.repository.CategoriaRepository;
import com.dulcemayor.repository.ProductoRepository;
import com.dulcemayor.repository.SubCategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CatalogoMayoristaService extends BusinessServiceTemplate {
    private final ProductoService productoService;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubCategoriaRepository subCategoriaRepository;
    private final CategoriaService categoriaService;
    private final SubCategoriaService subCategoriaService;
    private final VigenciaPrecioService precioService;

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listar(String query, String categoria, boolean includeOutOfStock) {
        return ejecutarCasoDeUso(() -> productoService.listarProductoActivo().stream()
                .filter(p -> includeOutOfStock || p.getStock() > 0)
                .filter(p -> query == null || query.isBlank() || texto(p).contains(query.toLowerCase(Locale.ROOT)))
                .filter(p -> categoria == null || categoria.isBlank() ||
                        p.getSubCategoria().getCategoria().getNombre().equalsIgnoreCase(categoria) ||
                        p.getSubCategoria().getNombre().equalsIgnoreCase(categoria))
                .map(this::toDto).toList());
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO buscar(String id) {
        return ejecutarCasoDeUso(() -> {
            Producto p = productoService.buscarProducto(id);
            if (p.isEliminado()) throw new IllegalArgumentException("Producto no encontrado: " + id);
            return toDto(p);
        });
    }

    @Transactional
    public ProductResponseDTO crear(ProductWriteDTO dto) {
        SubCategoria sub = findOrCreateSubcategory(dto.category());
        Producto p = productoService.crearProducto(dto.sku().trim(), dto.name().trim(), dto.description(),
                dto.pack(), dto.onSale(), null, sub.getId());
        setFields(p, dto);
        productoRepository.save(p);
        precioService.crearVigenciaPrecio(Date.valueOf(LocalDate.now()), null, dto.price().doubleValue(), p.getId());
        return toDto(p);
    }

    @Transactional
    public ProductResponseDTO actualizar(String id, ProductWriteDTO dto) {
        Producto p = productoService.buscarProducto(id);
        SubCategoria sub = findOrCreateSubcategory(dto.category());
        p.setNombre(dto.name().trim()); p.setDescripcion(dto.description()); p.setTalle(dto.pack());
        p.setSubCategoria(sub); setFields(p, dto); p.setEnOferta(dto.onSale());
        productoRepository.save(p);
        VigenciaPrecio current;
        try { current = precioService.buscarVigenciaPrecioVigente(id); }
        catch (IllegalArgumentException ex) { current = null; }
        if (current == null || BigDecimal.valueOf(current.getPrecio()).compareTo(dto.price()) != 0) {
            precioService.crearVigenciaPrecio(Date.valueOf(LocalDate.now()), null, dto.price().doubleValue(), id);
        }
        return toDto(p);
    }

    @Transactional
    public void eliminar(String id) { productoService.eliminarProducto(id); }

    @Transactional
    public ProductResponseDTO actualizarStock(String id, int stock) {
        if (stock < 0) throw new IllegalArgumentException("El stock no puede ser negativo");
        Producto p = productoService.buscarProducto(id);
        p.setStock(stock);
        productoRepository.save(p);
        return toDto(p);
    }

    @Transactional(readOnly = true)
    public List<String> categorias() {
        return categoriaRepository.findByEliminadoFalse().stream().map(Categoria::getNombre).sorted().toList();
    }

    public ProductResponseDTO toDto(Producto p) {
        BigDecimal price = BigDecimal.ZERO;
        try { price = BigDecimal.valueOf(precioService.buscarVigenciaPrecioVigente(p.getId()).getPrecio()); }
        catch (IllegalArgumentException ignored) { }
        int units = Math.max(1, p.getUnidadesPorBulto());
        String category = p.getSubCategoria() != null && p.getSubCategoria().getCategoria() != null
                ? p.getSubCategoria().getCategoria().getNombre() : "Golosinas";
        String image = p.getImagenUrl();
        if ((image == null || image.isBlank()) && p.getImagen() != null) image = "/api/images/" + p.getImagen().getId();
        return new ProductResponseDTO(p.getId(), p.getNombre(), p.getMarca(), category,
                p.getPresentacion(), units, price, price.divide(BigDecimal.valueOf(units), 2, java.math.RoundingMode.HALF_UP),
                p.getStock(), p.getStockMinimo(), p.getStock() > 0, image == null ? "" : image, p.getColorImagen(), p.getDescripcion(), p.isEnOferta() ? "Oferta" : null);
    }

    private void setFields(Producto p, ProductWriteDTO dto) {
        p.setMarca(dto.brand().trim()); p.setPresentacion(dto.pack().trim());
        p.setUnidadesPorBulto(dto.unitsPerPack()); p.setStock(dto.stock()); p.setStockMinimo(dto.stockMinimum());
        p.setImagenUrl(dto.image());
        if (dto.color() != null && dto.color().matches("#[0-9A-Fa-f]{6}")) p.setColorImagen(dto.color());
    }

    private SubCategoria findOrCreateSubcategory(String name) {
        String categoryName = name == null || name.isBlank() ? "Golosinas" : name.trim();
        Categoria category = categoriaRepository.findByNombreIgnoreCase(categoryName)
                .orElseGet(() -> categoriaService.crearCategoria(categoryName));
        return subCategoriaRepository.findByCategoriaIdAndEliminadoFalse(category.getId()).stream().findFirst()
                .orElseGet(() -> subCategoriaService.crearSubCategoria("General", category.getId()));
    }

    private String texto(Producto p) {
        return String.join(" ", p.getNombre() == null ? "" : p.getNombre(), p.getCodigo() == null ? "" : p.getCodigo(),
                p.getMarca() == null ? "" : p.getMarca(), p.getDescripcion() == null ? "" : p.getDescripcion(),
                p.getSubCategoria().getCategoria().getNombre()).toLowerCase(Locale.ROOT);
    }
}
