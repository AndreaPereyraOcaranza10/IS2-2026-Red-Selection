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

@Service
public class ProductoService extends BaseService<
        Producto, ProductoCrearDTO, ProductoRespuestaDTO> {

    private final ProductoRepository productoRepository;
    private final StockRepository stockRepository;
    private final SubCategoriaRepository subCategoriaRepository;
    private final ImagenRepository imagenRepository;
    private final VigenciaPrecioRepository vigenciaPrecioRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            StockRepository stockRepository,
            SubCategoriaRepository subCategoriaRepository,
            ImagenRepository imagenRepository,
            VigenciaPrecioRepository vigenciaPrecioRepository) {

        super(productoRepository);

        this.productoRepository = productoRepository;
        this.stockRepository = stockRepository;
        this.subCategoriaRepository = subCategoriaRepository;
        this.imagenRepository = imagenRepository;
        this.vigenciaPrecioRepository = vigenciaPrecioRepository;
    }

    // =====================================================
    // IMPLEMENTACION DE BASESERVICE
    // =====================================================

    @Override
    protected String nombreRecurso() {
        return "Producto";
    }

    @Override
    protected Producto aEntidad(ProductoCrearDTO dto) {

        return Producto.builder()
                .codigo(dto.codigo())
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .marca(dto.marca())
                .presentacion(dto.presentacion())
                .unidadesPorPresentacion(dto.unidadesPorPresentacion())
                .stockMinimo(dto.stockMinimo())
                .enOferta(dto.enOferta())
                .subCategoria(buscarSubCategoria(dto.subCategoriaId()))
                .imagen(buscarImagen(dto.imagenId()))
                .build();
    }

    @Override
    protected void aplicarCambios(
            Producto producto,
            ProductoCrearDTO dto) {

        producto.setNombre(dto.nombre());
        producto.setDescripcion(dto.descripcion());
        producto.setMarca(dto.marca());
        producto.setPresentacion(dto.presentacion());
        producto.setUnidadesPorPresentacion(dto.unidadesPorPresentacion());
        producto.setStockMinimo(dto.stockMinimo());
        producto.setEnOferta(dto.enOferta());

        producto.setSubCategoria(
                buscarSubCategoria(dto.subCategoriaId())
        );

        if (dto.imagenId() != null && !dto.imagenId().isBlank()) {
            producto.setImagen(buscarImagen(dto.imagenId()));
        }
    }

    @Override
    protected ProductoRespuestaDTO aRespuesta(Producto producto) {

        int stock = Math.toIntExact(
                stockRepository.stockActual(producto.getId())
        );

        return new ProductoRespuestaDTO(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getMarca(),
                producto.getPresentacion(),
                producto.getUnidadesPorPresentacion(),
                producto.getSubCategoria().getCategoria().getNombre(),
                producto.getSubCategoria().getNombre(),
                obtenerPrecioVigente(producto.getId()),
                stock,
                producto.getStockMinimo(),
                producto.isEnOferta(),
                stock > 0,
                producto.getImagen() != null
                        ? producto.getImagen().getId()
                        : null
        );
    }

    // =====================================================
    // VALIDACIONES
    // =====================================================

    @Override
    protected void validarCreacion(ProductoCrearDTO dto) {

        productoRepository.findByCodigoIgnoreCase(dto.codigo())
                .ifPresent(p -> {
                    throw new ReglaNegocioException(
                            "Ya existe un producto con ese código."
                    );
                });

        if (dto.precioInicial() == null ||
                dto.precioInicial().compareTo(BigDecimal.ZERO) <= 0) {

            throw new ReglaNegocioException(
                    "El precio inicial debe ser mayor a cero."
            );
        }

        if (dto.stockInicial() < 0) {
            throw new ReglaNegocioException(
                    "El stock inicial no puede ser negativo."
            );
        }

        if (dto.stockMinimo() < 0 ||
                dto.unidadesPorPresentacion() < 1) {

            throw new ReglaNegocioException(
                    "Los valores de stock mínimo y unidades por presentación no son válidos."
            );
        }
    }

    // =====================================================
    // ALTA DE PRODUCTO
    // =====================================================

    @Override
    @Transactional
    public ProductoRespuestaDTO crear(ProductoCrearDTO dto) {

        validarCreacion(dto);

        Producto producto = productoRepository.save(aEntidad(dto));

        // Crear precio inicial
        VigenciaPrecio precio = VigenciaPrecio.builder()
                .producto(producto)
                .fechaDesde(LocalDate.now())
                .precio(dto.precioInicial())
                .build();

        vigenciaPrecioRepository.save(precio);

        // Crear movimiento inicial de stock
        if (dto.stockInicial() > 0) {

            Stock stock = Stock.builder()
                    .producto(producto)
                    .movimiento(dto.stockInicial())
                    .cantidadActual(dto.stockInicial())
                    .tipoMovimiento(TipoMovimientoStock.INGRESO)
                    .fecha(LocalDateTime.now())
                    .build();

            stockRepository.save(stock);
        }

        return aRespuesta(producto);
    }

    // =====================================================
    // MODIFICACION DE PRODUCTO
    // =====================================================

    @Transactional
    public ProductoRespuestaDTO actualizar(
            String id,
            ProductoActualizarDTO dto) {

        Producto producto = buscarEntidad(id);

        producto.setNombre(dto.nombre());
        producto.setDescripcion(dto.descripcion());
        producto.setMarca(dto.marca());
        producto.setPresentacion(dto.presentacion());
        producto.setUnidadesPorPresentacion(dto.unidadesPorPresentacion());
        producto.setStockMinimo(dto.stockMinimo());
        producto.setEnOferta(dto.enOferta());

        producto.setSubCategoria(
                buscarSubCategoria(dto.subCategoriaId())
        );

        if (dto.imagenId() != null && !dto.imagenId().isBlank()) {
            producto.setImagen(buscarImagen(dto.imagenId()));
        }

        productoRepository.save(producto);

        return aRespuesta(producto);
    }

    // =====================================================
    // ACTUALIZACION DE PRECIO
    // =====================================================

    @Transactional
    public ProductoRespuestaDTO actualizarPrecio(
            String productoId,
            BigDecimal nuevoPrecio) {

        if (nuevoPrecio == null ||
                nuevoPrecio.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ReglaNegocioException(
                    "El precio debe ser mayor a cero."
            );
        }

        Producto producto = buscarEntidad(productoId);

        LocalDate hoy = LocalDate.now();

        List<VigenciaPrecio> vigentes =
                vigenciaPrecioRepository.vigentesEn(productoId, hoy);

        if (vigentes.size() > 1) {
            throw new ReglaNegocioException(
                    "El producto tiene más de un precio vigente."
            );
        }

        if (!vigentes.isEmpty()) {

            VigenciaPrecio anterior = vigentes.get(0);

            if (anterior.getFechaDesde().equals(hoy)) {

                anterior.setPrecio(nuevoPrecio);
                vigenciaPrecioRepository.save(anterior);

            } else {

                anterior.setFechaHasta(hoy.minusDays(1));
                vigenciaPrecioRepository.save(anterior);

                crearNuevaVigencia(producto, nuevoPrecio, hoy);
            }

        } else {
            crearNuevaVigencia(producto, nuevoPrecio, hoy);
        }

        return aRespuesta(producto);
    }

    private void crearNuevaVigencia(
            Producto producto,
            BigDecimal precio,
            LocalDate fecha) {

        VigenciaPrecio nueva = VigenciaPrecio.builder()
                .producto(producto)
                .fechaDesde(fecha)
                .precio(precio)
                .build();

        vigenciaPrecioRepository.save(nueva);
    }

    // =====================================================
    // MOVIMIENTOS DE STOCK
    // =====================================================

    @Transactional
    public ProductoRespuestaDTO registrarMovimientoStock(
            String productoId,
            MovimientoStockDTO dto) {

        Producto producto = productoRepository.buscarConBloqueo(productoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Producto", productoId
                        )
                );

        if (dto.tipoMovimiento() == null || dto.movimiento() == 0) {
            throw new ReglaNegocioException(
                    "El movimiento y su tipo son obligatorios."
            );
        }

        if (dto.tipoMovimiento() == TipoMovimientoStock.INGRESO
                && dto.movimiento() < 0) {

            throw new ReglaNegocioException(
                    "Un ingreso debe tener una cantidad positiva."
            );
        }

        if (dto.tipoMovimiento() == TipoMovimientoStock.VENTA
                && dto.movimiento() > 0) {

            throw new ReglaNegocioException(
                    "Una venta debe tener una cantidad negativa."
            );
        }

        long stockNuevo =
                stockRepository.stockActual(productoId) + dto.movimiento();

        if (stockNuevo < 0) {
            throw new ReglaNegocioException(
                    "No hay stock suficiente para realizar este movimiento."
            );
        }

        Stock movimiento = Stock.builder()
                .producto(producto)
                .movimiento(dto.movimiento())
                .cantidadActual(Math.toIntExact(stockNuevo))
                .tipoMovimiento(dto.tipoMovimiento())
                .fecha(LocalDateTime.now())
                .build();

        stockRepository.save(movimiento);

        return aRespuesta(producto);
    }

    // =====================================================
    // CONSULTAS
    // =====================================================

    @Transactional(readOnly = true)
    public List<ProductoRespuestaDTO> listarPorSubCategoria(
            String subCategoriaId) {

        buscarSubCategoria(subCategoriaId);

        return productoRepository
                .findBySubCategoriaIdAndEliminadoFalse(subCategoriaId)
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    private BigDecimal obtenerPrecioVigente(String productoId) {

        List<VigenciaPrecio> precios =
                vigenciaPrecioRepository.vigentesEn(
                        productoId,
                        LocalDate.now()
                );

        if (precios.size() > 1) {
            throw new ReglaNegocioException(
                    "El producto tiene más de un precio vigente."
            );
        }

        return precios.isEmpty()
                ? null
                : precios.get(0).getPrecio();
    }

    // =====================================================
    // BUSQUEDA DE RELACIONES
    // =====================================================

    private SubCategoria buscarSubCategoria(String id) {

        return subCategoriaRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Subcategoría", id
                        )
                );
    }

    private Imagen buscarImagen(String id) {

        if (id == null || id.isBlank()) {
            return null;
        }

        return imagenRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Imagen", id
                        )
                );
    }
}
