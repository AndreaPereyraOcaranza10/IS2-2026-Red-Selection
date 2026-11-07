package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import redSelection.dulceMayor.dto.CatalogoProductoDTO;
import redSelection.dulceMayor.dto.CategoriaRespuestaDTO;
import redSelection.dulceMayor.dto.ProductoRespuestaDTO;
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

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public CatalogoService(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    @Transactional(readOnly = true)
    public List<CatalogoProductoDTO> listar(String busqueda, String categoria, boolean incluirSinStock) {
        String texto = normalizar(busqueda);
        String filtroCategoria = normalizar(categoria);

        return productoService.listar().stream()
                .filter(p -> incluirSinStock || p.disponible())                       // sin stock no se muestra
                .filter(p -> texto.isEmpty() || coincideTexto(p, texto))
                .filter(p -> filtroCategoria.isEmpty() || coincideCategoria(p, filtroCategoria))
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogoProductoDTO buscar(String id) {
        return convertirADTO(productoService.obtener(id));
    }

    @Transactional(readOnly = true)
    public List<String> categorias() {
        return categoriaService.listar().stream()
                .map(CategoriaRespuestaDTO::nombre)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private CatalogoProductoDTO convertirADTO(ProductoRespuestaDTO p) {
        BigDecimal precio = p.precio();
        int unidades = p.unidadesPorPresentacion();
        BigDecimal precioPorUnidad = precio != null && unidades > 0
                ? precio.divide(BigDecimal.valueOf(unidades), 2, RoundingMode.HALF_UP)
                : null;

        return new CatalogoProductoDTO(
                p.id(), p.nombre(), p.marca(), p.categoria(), p.subCategoria(),
                p.presentacion(), unidades, precio, precioPorUnidad,
                p.stockActual(), p.stockMinimo(), p.disponible(), p.enOferta(),
                p.descripcion(), p.imagenId());
    }

    private boolean coincideTexto(ProductoRespuestaDTO p, String busqueda) {
        String texto = String.join(" ",
                valor(p.nombre()), valor(p.codigo()), valor(p.marca()), valor(p.descripcion()),
                valor(p.subCategoria()), valor(p.categoria())).toLowerCase(Locale.ROOT);
        return texto.contains(busqueda);
    }

    private boolean coincideCategoria(ProductoRespuestaDTO p, String categoria) {
        return normalizar(p.categoria()).equals(categoria) || normalizar(p.subCategoria()).equals(categoria);
    }

    private String normalizar(String texto) { return texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT); }

    private String valor(String texto) { return texto == null ? "" : texto; }
}
