package com.tienda.zero.service;

import com.tienda.zero.model.Localidad;

import java.util.List;

public interface LocalidadService {

    void crearLocalidad(String nombre, String codigoPostal, String idDepartamento);

    void validar(String nombre, String codigoPostal, String idDepartamento);

    Localidad buscarLocalidad(String id);

    Localidad buscarLocalidadPorNombre(String nombre);

    Localidad buscarLocalidadPorCodigoPostal(String codigoPostal);

    void modificarLocalidad(String id, String nombre, String codigoPostal, String idDepartamento);

    void eliminarLocalidad(String id);

    List<Localidad> listarLocalidad(String idDepartamento);

    List<Localidad> listarLocalidadActivo(String idDepartamento);
}