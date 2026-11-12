package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import redSelection.dulceMayor.dto.*;
import redSelection.dulceMayor.enums.TipoMovimientoStock;
import redSelection.dulceMayor.exception.*;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ProductoService extends BaseService<Producto, ProductoCrearDTO, ProductoRespuestaDTO> {

    private final ProductoRepository productoRepository;
    private final SubCategoriaService subCategoriaService;
    private final ImagenService imagenService;
    private final StockService stockService;
    private final PrecioService precioService;

    public ProductoService(ProductoRepository productoRepository, SubCategoriaService subCategoriaService,
                           ImagenService imagenService, StockService stockService, PrecioService precioService) {
        super(productoRepository);
        this.productoRepository = productoRepository;
        this.subCategoriaService = subCategoriaService;
        this.imagenService = imagenService;
        this.stockService = stockService;
        this.precioService = precioService;
    }

    // ---------- Pasos del Template Method ----------

    @Override protected String nombreRecurso() { return "Producto"; }

    @Override
    protected Producto aEntidad(ProductoCrearDTO dto) {
        return Producto.builder()
                .codigo(dto.codigo().trim()).nombre(dto.nombre().trim())
                .descripcion(dto.descripcion()).marca(dto.marca())
                .presentacion(dto.presentacion()).unidadesPorPresentacion(dto.unidadesPorPresentacion())
                .stockMinimo(dto.stockMinimo()).enOferta(dto.enOferta())
                .subCategoria(subCategoriaService.obtenerEntidad(dto.subCategoriaId()))
                .imagen(imagenService.buscarOpcional(dto.imagenId()))
                .build();
    }

    /** El código no se modifica una vez creado. Precio y stock iniciales no aplican al actualizar. */
    @Override
    protected void aplicarCambios(Producto producto, ProductoCrearDTO dto) {
        producto.setNombre(dto.nombre().trim());
        producto.setDescripcion(dto.descripcion());
        producto.setMarca(dto.marca());
        producto.setPresentacion(dto.presentacion());
        producto.setUnidadesPorPresentacion(dto.unidadesPorPresentacion());
        producto.setStockMinimo(dto.stockMinimo());
        producto.setEnOferta(dto.enOferta());
        producto.setSubCategoria(subCategoriaService.obtenerEntidad(dto.subCategoriaId()));
        if (dto.imagenId() != null && !dto.imagenId().isBlank()) {
            producto.setImagen(imagenService.obtenerEntidad(dto.imagenId()));
        }
    }

    @Override
    protected ProductoRespuestaDTO aRespuesta(Producto producto) {
        return armarRespuesta(producto, stockService.stockActual(producto.getId()));
    }

    // ---------- Hooks ----------

    @Override
    protected void validarCreacion(ProductoCrearDTO dto) {
        productoRepository.findByCodigoIgnoreCase(dto.codigo().trim()).ifPresent(p -> {
            throw new ReglaNegocioException("Ya existe un producto con ese código.");
        });
        if (dto.precioInicial() == null || dto.precioInicial().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El precio inicial debe ser mayor a cero.");
        }
        if (dto.stockInicial() < 0) {
            throw new ReglaNegocioException("El stock inicial no puede ser negativo.");
        }
    }

    /** Alta completa: el producto nace con su primera vigencia de precio y su ingreso inicial de stock. */
    @Override
    protected void despuesDeCrear(Producto producto, ProductoCrearDTO dto) {
        precioService.fijarPrecio(producto, dto.precioInicial(), LocalDate.now());
        if (dto.stockInicial() > 0) {
            stockService.registrar(producto, TipoMovimientoStock.INGRESO, dto.stockInicial(), null);
        }
    }

    // ---------- Listados ----------

    /** Trae el stock de todos los productos en una sola consulta (el base haría una por producto). */
    @Override
    @Transactional(readOnly = true)
    public List<ProductoRespuestaDTO> listar() {
        Map<String, Long> stock = stockService.stockPorProducto();
        return buscarTodos().stream().map(p -> armarRespuesta(p, stock.getOrDefault(p.getId(), 0L))).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoRespuestaDTO> listarPorSubCategoria(String subCategoriaId) {
        subCategoriaService.obtenerEntidad(subCategoriaId);   // 404 si no existe
        return productoRepository.findBySubCategoriaIdAndEliminadoFalse(subCategoriaId).stream()
                .map(this::aRespuesta).toList();
    }

    // ---------- Precio y stock ----------

    @Transactional
    public ProductoRespuestaDTO actualizarPrecio(String productoId, BigDecimal nuevoPrecio) {
        Producto producto = buscarEntidad(productoId);
        precioService.fijarPrecio(producto, nuevoPrecio, LocalDate.now());
        return aRespuesta(producto);
    }

    /** Lock de fila: mientras dure la transacción nadie más modifica el stock. Lo usará también Facturación. */
    @Transactional
    public Producto obtenerConBloqueo(String id) {
        return productoRepository.buscarConBloqueo(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(nombreRecurso(), id));
    }

    /** Manuales solo INGRESO y AJUSTE; las ventas las genera la facturación para que cada salida quede ligada a su factura. */
    @Transactional
    public ProductoRespuestaDTO registrarMovimientoStock(String productoId, MovimientoStockDTO dto) {
        TipoMovimientoStock tipo = dto.tipoMovimiento();
        if (tipo != TipoMovimientoStock.INGRESO && tipo != TipoMovimientoStock.AJUSTE) {
            throw new ReglaNegocioException("Solo se registran manualmente ingresos o ajustes; las ventas salen de la facturación.");
        }
        if (tipo == TipoMovimientoStock.INGRESO && dto.movimiento() <= 0) {
            throw new ReglaNegocioException("Un ingreso debe tener una cantidad positiva.");
        }
        Producto producto = obtenerConBloqueo(productoId);
        stockService.registrar(producto, tipo, dto.movimiento(), null);
        return aRespuesta(producto);
    }

    @Transactional(readOnly = true)
    public List<MovimientoStockRespuestaDTO> historialStock(String productoId) {
        buscarEntidad(productoId);
        return stockService.historial(productoId);
    }

    private ProductoRespuestaDTO armarRespuesta(Producto p, long stock) {
        int stockActual = Math.toIntExact(stock);
        return new ProductoRespuestaDTO(
                p.getId(), p.getCodigo(), p.getNombre(), p.getDescripcion(), p.getMarca(),
                p.getPresentacion(), p.getUnidadesPorPresentacion(),
                p.getSubCategoria().getCategoria().getId(), p.getSubCategoria().getCategoria().getNombre(),
                p.getSubCategoria().getId(), p.getSubCategoria().getNombre(),
                precioService.precioVigente(p.getId(), LocalDate.now()).orElse(null),
                stockActual, p.getStockMinimo(), p.isEnOferta(), stockActual > 0,
                p.getImagen() != null ? p.getImagen().getId() : null);
    }

    /** Lo consulta SubCategoriaService antes de dar de baja una subcategoría. */
    @Transactional(readOnly = true)
    public boolean existenPorSubCategoria(String subCategoriaId) {
        return !productoRepository.findBySubCategoriaIdAndEliminadoFalse(subCategoriaId).isEmpty();
    }

    /** Para el newsletter: los productos marcados en oferta (entidades, uso interno entre services). */
    @Transactional(readOnly = true)
    public List<Producto> enOferta() {
        return productoRepository.findByEnOfertaTrueAndEliminadoFalse();
    }

}
