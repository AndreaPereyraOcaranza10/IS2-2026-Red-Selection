package com.tienda.zero.service;

import com.tienda.zero.model.Producto;
import com.tienda.zero.model.VigenciaPrecio;

import java.util.List;


public interface GestionProductoService {

    Producto crearProductoConPrecio(String codigo, String nombre, String descripcion, String talle,
                                    boolean enOferta, String idSubCategoria, double precio,
                                    String nombreImagen, String mimeImagen, byte[] contenidoImagen);

    Producto modificarProducto(String id, String nombre, String descripcion, String talle,
                               boolean enOferta, String idSubCategoria,
                               String nombreImagen, String mimeImagen, byte[] contenidoImagen);

    void cambiarPrecio(String idProducto, double nuevoPrecio);

    List<VigenciaPrecio> listarPrecios(String idProducto);

}