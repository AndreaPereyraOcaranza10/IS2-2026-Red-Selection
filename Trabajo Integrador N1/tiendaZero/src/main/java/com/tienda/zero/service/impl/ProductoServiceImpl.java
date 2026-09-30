package com.tienda.zero.service.impl;

import com.tienda.zero.model.Imagen;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.SubCategoria;
import com.tienda.zero.repository.ProductoRepository;
import com.tienda.zero.service.ImagenService;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.StockService;
import com.tienda.zero.service.SubCategoriaService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final SubCategoriaService subCategoriaService;
    private final ImagenService imagenService;
    private final StockService stockService;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               SubCategoriaService subCategoriaService,
                               ImagenService imagenService, StockService stockService) {
        this.productoRepository = productoRepository;
        this.subCategoriaService = subCategoriaService;
        this.imagenService = imagenService;
        this.stockService = stockService;

    }

    @Override
    @Transactional
    public Producto crearProducto(String codigo, String nombre, String descripcion, String talle,
                                  boolean enOferta, String idImagen, String idSubCategoria) {
        validarProducto(codigo, nombre, descripcion, talle, enOferta, idImagen, idSubCategoria);
        SubCategoria subCategoria = buscarSubCategoriaActiva(idSubCategoria);
        Imagen imagen = buscarImagenOpcional(idImagen);

        Producto producto = new Producto();
        producto.setCodigo(codigo.trim());
        producto.setNombre(nombre.trim());
        producto.setDescripcion(limpiarOpcional(descripcion));
        producto.setTalle(limpiarOpcional(talle));
        producto.setEnOferta(enOferta);
        producto.setEliminado(false);
        producto.setSubCategoria(subCategoria);
        producto.setImagen(imagen);
        return productoRepository.save(producto);
    }

    @Override
    public void validarProducto(String codigo, String nombre, String descripcion, String talle,
                                boolean enOferta, String idImagen, String idSubCategoria) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del producto es obligatorio");
        }

        validarNombreYSubCategoria(nombre, idSubCategoria);

        Optional<Producto> existente = productoRepository.findByCodigoIgnoreCase(codigo.trim());
        if (existente.isPresent()) {
            if (existente.get().isEliminado()) {
                throw new IllegalArgumentException("Ya existe un producto eliminado con ese código");
            }
            throw new IllegalArgumentException("Ya existe un producto con ese código");
        }
    }

    @Override
    @Transactional
    public Producto modificarProducto(String id, String nombre, String descripcion, String talle,
                                      boolean enOferta, String idImagen, String idSubCategoria) {

        validarNombreYSubCategoria(nombre, idSubCategoria);
        Producto producto = buscarProducto(id);

        if (producto.isEliminado()) {
            throw new IllegalArgumentException("No se puede modificar un producto eliminado");
        }

        SubCategoria subCategoria = buscarSubCategoriaActiva(idSubCategoria);
        Imagen nuevaImagen = buscarImagenOpcional(idImagen);

        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setTalle(talle);
        producto.setEnOferta(enOferta);
        producto.setSubCategoria(subCategoria);
        if (nuevaImagen != null) {
            producto.setImagen(nuevaImagen);
        }

        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public void eliminarProducto(String id) {
        Producto producto = buscarProducto(id);

        int stock = stockService.calcularStockActual(id);
        if (stock > 0) {
            throw new IllegalArgumentException("No se puede eliminar el producto \"" + producto.getNombre()
                    + "\": todavía tiene " + stock + " unidades en stock");
        }

        producto.setEliminado(true);
        productoRepository.save(producto);
    }

    @Override
    @Transactional
    public Producto actualizarOferta(String id, double porcentajeDescuento) {
        Producto producto = buscarProducto(id);
        if (producto.isEliminado()) {
            throw new IllegalArgumentException("No se puede cambiar la oferta de un producto eliminado");
        }
        if (!Double.isFinite(porcentajeDescuento) || porcentajeDescuento < 0 || porcentajeDescuento >= 100) {
            throw new IllegalArgumentException("El descuento debe ser mayor o igual a 0 y menor a 100");
        }
        producto.setPorcentajeDescuento(porcentajeDescuento);
        producto.setEnOferta(porcentajeDescuento > 0);
        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public Producto actualizarStockIdeal(String id, int stockIdeal) {
        if (stockIdeal <= 0) {
            throw new IllegalArgumentException("El stock ideal debe ser mayor que cero");
        }
        Producto producto = buscarProducto(id);
        if (producto.isEliminado()) {
            throw new IllegalArgumentException("No se puede actualizar el stock ideal de un producto eliminado");
        }
        producto.setStockIdeal(stockIdeal);
        return productoRepository.save(producto);
    }

    @Override
    public List<Producto> listarProducto() {

        return productoRepository.findAll();
    }

    @Override
    public List<Producto> listarProductoActivo() {

        return productoRepository.findByEliminadoFalse();
    }

    @Override
    public Producto buscarProductoPorNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        Optional<Producto> resultado = productoRepository.findFirstByNombreIgnoreCase(nombre.trim());

        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("Producto no encontrado: " + nombre);
        }
        return resultado.get();
    }

    @Override
    public Producto buscarProductoPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del producto es obligatorio");
        }
        Optional<Producto> resultado = productoRepository.findByCodigoIgnoreCase(codigo.trim());
        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("Producto no encontrado: " + codigo);
        }
        return resultado.get();
    }

    @Override
    public Producto buscarProducto(String id) {
        Optional<Producto> resultado = productoRepository.findById(id);

        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("Producto no encontrado: " + id);
        }
        return resultado.get();
    }

    private void validarNombreYSubCategoria(String nombre, String idSubCategoria) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (idSubCategoria == null || idSubCategoria.isBlank()) {
            throw new IllegalArgumentException("El producto debe pertenecer a una subcategoría");
        }
    }

    private SubCategoria buscarSubCategoriaActiva(String idSubCategoria) {
        SubCategoria subCategoria = subCategoriaService.buscarSubCategoria(idSubCategoria);
        if (subCategoria.isEliminado()) {
            throw new IllegalArgumentException("La subcategoría está eliminada");
        }
        return subCategoria;
    }

    /** La imagen es opcional: sin id devuelve null, con id la busca y exige que no esté eliminada. */
    private Imagen buscarImagenOpcional(String idImagen) {
        if (idImagen == null || idImagen.isBlank()) {
            return null;
        }
        Imagen imagen = imagenService.buscarImagen(idImagen);
        if (imagen.isEliminado()) {
            throw new IllegalArgumentException("La imagen está eliminada");
        }
        return imagen;
    }

    /** Los campos opcionales en blanco se guardan como null, no como texto vacío. */
    private String limpiarOpcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

}
