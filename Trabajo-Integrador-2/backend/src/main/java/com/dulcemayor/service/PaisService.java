package com.dulcemayor.service;

import com.dulcemayor.model.Pais;

import java.util.List;

public interface PaisService {

    void crearPais(String nombre);

    void validar(String nombre);

    Pais buscarPais(String id);

    Pais buscarPaisPorNombre(String nombre);

    void modificarPais(String id, String nombre);

    void eliminarPais(String id);

    List<Pais> listarPais();

    List<Pais> listarPaisActivo();
}