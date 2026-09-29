package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoImagen;
import com.tienda.zero.model.Imagen;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.VigenciaPrecio;
import com.tienda.zero.service.GestionProductoService;
import com.tienda.zero.service.ImagenService;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.VigenciaPrecioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class GestionProductoServiceImpl implements GestionProductoService {

    private final ProductoService productoService;
    private final VigenciaPrecioService vigenciaPrecioService;
    private final ImagenService imagenService;

    public GestionProductoServiceImpl(ProductoService productoService,
                                      VigenciaPrecioService vigenciaPrecioService,
                                      ImagenService imagenService) {
        this.productoService = productoService;
        this.vigenciaPrecioService = vigenciaPrecioService;
        this.imagenService = imagenService;
    }

    @Override
    @Transactional
    public Producto crearProductoConPrecio(String codigo, String nombre, String descripcion, String talle,
                                           boolean enOferta, String idSubCategoria, double precio,
                                           String nombreImagen, String mimeImagen, byte[] contenidoImagen) {
        // El precio se valida antes de crear nada, para fallar rápido y con un mensaje claro.
        validarPrecio(precio);

        String idImagen = crearImagenSiHayContenido(nombreImagen, mimeImagen, contenidoImagen);

        Producto producto = productoService.crearProducto(codigo, nombre, descripcion, talle,
                enOferta, idImagen, idSubCategoria);
        vigenciaPrecioService.crearVigenciaPrecio(hoy(), null, precio, producto.getId());
        return producto;
    }

    @Override
    @Transactional
    public Producto modificarProducto(String id, String nombre, String descripcion, String talle,
                                      boolean enOferta, String idSubCategoria,
                                      String nombreImagen, String mimeImagen, byte[] contenidoImagen) {
        String idImagen = crearImagenSiHayContenido(nombreImagen, mimeImagen, contenidoImagen);
        return productoService.modificarProducto(id, nombre, descripcion, talle, enOferta, idImagen, idSubCategoria);
    }

    @Override
    @Transactional
    public void cambiarPrecio(String idProducto, double nuevoPrecio) {
        validarPrecio(nuevoPrecio);

        Producto producto = productoService.buscarProducto(idProducto);
        if (producto.isEliminado()) {
            throw new IllegalArgumentException("No se puede cambiar el precio de un producto eliminado");
        }

        vigenciaPrecioService.crearVigenciaPrecio(hoy(), null, nuevoPrecio, idProducto);
    }

    @Override
    public List<VigenciaPrecio> listarPrecios(String idProducto) {
        productoService.buscarProducto(idProducto);   // falla con un mensaje claro si no existe

        List<VigenciaPrecio> resultado = new ArrayList<>();
        for (VigenciaPrecio vigencia : vigenciaPrecioService.listarVigenciaPrecioActivo()) {
            if (vigencia.getProducto().getId().equals(idProducto)) {
                resultado.add(vigencia);
            }
        }

        // Más nuevo primero; si dos empiezan el mismo día, primero la que sigue abierta (la vigente)
        Comparator<VigenciaPrecio> porFecha = Comparator
                .comparing(VigenciaPrecio::getFechaDesde)
                .thenComparing(v -> v.getFechaHasta() == null);
        resultado.sort(porFecha.reversed());
        return resultado;
    }

    private String crearImagenSiHayContenido(String nombreImagen, String mimeImagen, byte[] contenidoImagen) {
        if (contenidoImagen == null || contenidoImagen.length == 0) {
            return null;
        }
        Imagen imagen = imagenService.crearImagen(nombreImagen, mimeImagen, contenidoImagen, TipoImagen.PRODUCTO);
        return imagen.getId();
    }

    private void validarPrecio(double precio) {
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
    }

    private Date hoy() {
        return new Date(System.currentTimeMillis());
    }
}