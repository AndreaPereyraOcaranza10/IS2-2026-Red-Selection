package com.tienda.zero.service;

import com.tienda.zero.model.VigenciaPrecio;

import java.sql.Date;
import java.util.List;

public interface VigenciaPrecioService {

    void crearVigenciaPrecio(Date fechaDesde, Date fechaHasta, double precio, String idProducto);

    void validar(Date fechaDesde, Date fechaHasta, double precio, String idProducto);

    VigenciaPrecio buscarVigenciaPrecio(String id);

    void modificarVigenciaPrecio(String id, Date fechaDesde, Date fechaHasta, double precio, String idProducto);

    void eliminarVigenciaPrecio(String id);

    List<VigenciaPrecio> listarVigenciaPrecio();

    List<VigenciaPrecio> listarVigenciaPrecioActivo();

    VigenciaPrecio buscarVigenciaPrecioVigente(String idProducto);
}