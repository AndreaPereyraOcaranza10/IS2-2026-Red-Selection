package com.tienda.zero.service;

import com.tienda.zero.model.Producto;

import java.util.List;

public interface ProductoService {
    Producto crearProducto(String codigo, String nombre, String descripcion, String talle,
                           boolean enOferta, String idImagen, String idSubCategoria);
    void validarProducto(String codigo, String nombre, String descripcion, String talle,
                         boolean enOferta, String idImagen, String idSubCategoria);
    Producto modificarProducto(String id, String nombre, String descripcion, String talle,
                               boolean enOferta, String idImagen, String idSubCategoria);
    void eliminarProducto(String id);
    List<Producto> listarProducto();
    List<Producto> listarProductoActivo();
    Producto buscarProductoPorNombre(String nombre);
    Producto buscarProductoPorCodigo(String codigo);
    Producto buscarProducto(String id);
}
