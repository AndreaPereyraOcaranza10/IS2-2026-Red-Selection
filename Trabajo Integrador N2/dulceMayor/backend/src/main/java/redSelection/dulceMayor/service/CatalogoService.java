package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import redSelection.dulceMayor.dto.CatalogoProductoDTO;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Producto;
import redSelection.dulceMayor.model.VigenciaPrecio;
import redSelection.dulceMayor.repository.CategoriaRepository;
import redSelection.dulceMayor.repository.ProductoRepository;
import redSelection.dulceMayor.repository.StockRepository;
import redSelection.dulceMayor.repository.VigenciaPrecioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class CatalogoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final StockRepository stockRepository;
    private final VigenciaPrecioRepository vigenciaPrecioRepository;

    public CatalogoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            StockRepository stockRepository,
            VigenciaPrecioRepository vigenciaPrecioRepository) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.stockRepository = stockRepository;
        this.vigenciaPrecioRepository = vigenciaPrecioRepository;
    }

    // =====================================================
    // LISTAR PRODUCTOS DEL CATALOGO
    // =====================================================

    @Transactional(readOnly = true)
    public List<CatalogoProductoDTO> listar(
            String busqueda,
            String categoria,
            boolean incluirSinStock) {

        String textoBusqueda = normalizar(busqueda);
        String filtroCategoria = normalizar(categoria);

        return productoRepository.findByEliminadoFalse()
                .stream()

                // Filtrar por disponibilidad
                .filter(producto ->
                        incluirSinStock ||
                                obtenerStock(producto.getId()) > 0
                )

                // Filtrar por texto
                .filter(producto ->
                        textoBusqueda.isEmpty() ||
                                coincideTexto(producto, textoBusqueda)
                )

                // Filtrar por categoría o subcategoría
                .filter(producto ->
                        filtroCategoria.isEmpty() ||
                                coincideCategoria(producto, filtroCategoria)
                )

                .map(this::convertirADTO)
                .toList();
    }

    // =====================================================
    // BUSCAR UN PRODUCTO
    // =====================================================

    @Transactional(readOnly = true)
    public CatalogoProductoDTO buscar(String id) {

        Producto producto = productoRepository
                .findByIdAndEliminadoFalse(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Producto", id
                        )
                );

        return convertirADTO(producto);
    }

    // =====================================================
    // LISTAR CATEGORIAS
    // =====================================================

    @Transactional(readOnly = true)
    public List<String> categorias() {

        return categoriaRepository.findByEliminadoFalse()
                .stream()
                .map(categoria -> categoria.getNombre())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    // =====================================================
    // CONVERSION DE PRODUCTO A DTO
    // =====================================================

    private CatalogoProductoDTO convertirADTO(Producto producto) {

        BigDecimal precio = obtenerPrecio(producto.getId());

        long stock = obtenerStock(producto.getId());

        int unidades = producto.getUnidadesPorPresentacion();

        BigDecimal precioPorUnidad = precio != null && unidades > 0
                ? precio.divide(
                BigDecimal.valueOf(unidades),
                2,
                RoundingMode.HALF_UP
        )
                : null;

        return new CatalogoProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getMarca(),
                producto.getSubCategoria()
                        .getCategoria()
                        .getNombre(),
                producto.getSubCategoria().getNombre(),
                producto.getPresentacion(),
                unidades,
                precio,
                precioPorUnidad,
                stock,
                producto.getStockMinimo(),
                stock > 0,
                producto.isEnOferta(),
                producto.getDescripcion(),
                producto.getImagen() != null
                        ? producto.getImagen().getId()
                        : null
        );
    }

    // =====================================================
    // OBTENER PRECIO VIGENTE
    // =====================================================

    private BigDecimal obtenerPrecio(String productoId) {

        List<VigenciaPrecio> vigencias =
                vigenciaPrecioRepository.vigentesEn(
                        productoId,
                        LocalDate.now()
                );

        if (vigencias.size() > 1) {
            throw new ReglaNegocioException(
                    "El producto tiene más de un precio vigente: "
                            + productoId
            );
        }

        return vigencias.isEmpty()
                ? null
                : vigencias.get(0).getPrecio();
    }

    // =====================================================
    // OBTENER STOCK ACTUAL
    // =====================================================

    private long obtenerStock(String productoId) {

        return stockRepository.stockActual(productoId);
    }

    // =====================================================
    // BUSQUEDA POR TEXTO
    // =====================================================

    private boolean coincideTexto(
            Producto producto,
            String busqueda) {

        String texto = String.join(" ",
                valor(producto.getNombre()),
                valor(producto.getCodigo()),
                valor(producto.getMarca()),
                valor(producto.getDescripcion()),
                valor(producto.getSubCategoria().getNombre()),
                valor(producto.getSubCategoria()
                        .getCategoria().getNombre())
        ).toLowerCase(Locale.ROOT);

        return texto.contains(busqueda);
    }

    // =====================================================
    // FILTRAR POR CATEGORIA
    // =====================================================

    private boolean coincideCategoria(
            Producto producto,
            String categoria) {

        String nombreCategoria = normalizar(
                producto.getSubCategoria()
                        .getCategoria().getNombre()
        );

        String nombreSubCategoria = normalizar(
                producto.getSubCategoria().getNombre()
        );

        return nombreCategoria.equals(categoria) ||
                nombreSubCategoria.equals(categoria);
    }

    // =====================================================
    // UTILIDADES
    // =====================================================

    private String normalizar(String texto) {

        return texto == null
                ? ""
                : texto.trim().toLowerCase(Locale.ROOT);
    }

    private String valor(String texto) {

        return texto == null ? "" : texto;
    }
}
