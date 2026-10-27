package com.tienda.zero.service;

import com.tienda.zero.model.Provincia;

import java.util.List;

public interface ProvinciaService {

    void crearProvincia(String nombre, String idPais);

    void validar(String nombre, String idPais);

    Provincia buscarProvincia(String id);

    Provincia buscarProvinciaPorNombre(String nombre);

    void modificarProvincia(String id, String nombre, String idPais);

    void eliminarProvincia(String id);

    List<Provincia> listarProvincia(String idPais);

    List<Provincia> listarProvinciaActivo(String idPais);
}