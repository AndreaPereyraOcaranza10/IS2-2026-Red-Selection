package com.tienda.zero.service;

import com.tienda.zero.model.Departamento;

import java.util.List;

public interface DepartamentoService {

    void crearDepartamento(String nombre, String idProvincia);

    void validar(String nombre, String idProvincia);

    Departamento buscarDepartamento(String id);

    Departamento buscarDepartamentoPorNombre(String nombre);

    void modificarDepartamento(String id, String nombre, String idProvincia);

    void eliminarDepartamento(String id);

    List<Departamento> listarDepartamento(String idProvincia);

    List<Departamento> listarDepartamentoActivo(String idProvincia);
}