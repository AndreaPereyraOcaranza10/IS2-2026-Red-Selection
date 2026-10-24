package com.tienda.zero.service;

import com.tienda.zero.model.Nacionalidad;

import java.util.List;

public interface NacionalidadService {

    Nacionalidad crearNacionalidad(String nombre);

    void validar(String nombre);

    Nacionalidad buscarNacionalidad(String id);

    Nacionalidad buscarNacionalidadPorNombre(String nombre);

    Nacionalidad modificarNacionalidad(String id, String nombre);

    void eliminarNacionalidad(String id);

    List<Nacionalidad> listarNacionalidad();

    List<Nacionalidad> listarNacionalidadActiva();
}