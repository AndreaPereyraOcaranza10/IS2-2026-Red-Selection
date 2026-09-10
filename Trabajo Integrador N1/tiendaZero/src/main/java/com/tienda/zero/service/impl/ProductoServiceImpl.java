package com.tienda.zero.service.impl;

import com.tienda.zero.model.Producto;
import com.tienda.zero.model.SubCategoria;
import com.tienda.zero.repository.ProductoRepository;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.SubCategoriaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final SubCategoriaService subCategoriaService;
    //private final ImagenService imagenService;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               SubCategoriaService subCategoriaService
                               /*,ImagenService imagenService*/) {
        this.productoRepository = productoRepository;
        this.subCategoriaService = subCategoriaService;
        //this.imagenService = imagenService;
    }

    @Override
    public Producto crearProducto(String codigo, String nombre, String descripcion, String talle,
                                  boolean enOferta, String idImagen, String idSubCategoria) {
        validarProducto(codigo, nombre, descripcion, talle, enOferta, idImagen, idSubCategoria);
        SubCategoria subCategoria = subCategoriaService.buscarSubCategoria(idSubCategoria);

        Producto producto = Producto.builder()
                .codigo(codigo)
                .nombre(nombre)
                .descripcion(descripcion)
                .talle(talle)
                .enOferta(enOferta)
                .subCategoria(subCategoria)
                //.imagen(idImagen)
                .build();

        return productoRepository.save(producto);
    }

    @Override
    public void validarProducto(String codigo, String nombre, String descripcion, String talle,
                                boolean enOferta, String idImagen, String idSubCategoria) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del producto es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (idSubCategoria == null || idSubCategoria.isBlank()) {
            throw new IllegalArgumentException("El producto debe pertenecer a una subcategoría");
        }
        productoRepository.findByCodigoIgnoreCase(codigo).ifPresent(p -> {
            throw new IllegalArgumentException("Ya existe un producto con ese código");
        });
        // Valida que la subcategoría exista
        subCategoriaService.buscarSubCategoria(idSubCategoria);
    }

    @Override
    public Producto modificarProducto(String id, String nombre, String descripcion, String talle,
                                      boolean enOferta, String idImagen, String idSubCategoria) {
        Producto producto = buscarProducto(id);
        SubCategoria subCategoria = subCategoriaService.buscarSubCategoria(idSubCategoria);
        //Imagen imagen = idImagen != null ? imagenService.buscarImagen(idImagen) : null;

        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setTalle(talle);
        producto.setEnOferta(enOferta);
        producto.setSubCategoria(subCategoria);
        //producto.setImagen(imagen);

        return productoRepository.save(producto);
    }

    @Override
    public void eliminarProducto(String id) {
        Producto producto = buscarProducto(id);
        producto.setEliminado(true);
        productoRepository.save(producto);
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
        return productoRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + nombre));
    }

    @Override
    public Producto buscarProductoPorCodigo(String codigo) {
        return productoRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + codigo));
    }

    @Override
    public Producto buscarProducto(String id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));
    }
}