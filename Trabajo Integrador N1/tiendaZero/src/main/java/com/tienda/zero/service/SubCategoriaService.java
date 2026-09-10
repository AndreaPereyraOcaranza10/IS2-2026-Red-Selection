package com.tienda.zero.service;


import com.tienda.zero.model.SubCategoria;
import java.util.List;

public interface SubCategoriaService {
    SubCategoria crearSubCategoria(String nombre, String idCategoria);
    void validar(String nombre, String idCategoria);
    SubCategoria buscarSubCategoria(String id);
    SubCategoria buscarSubCategoriaPorNombre(String nombre);
    SubCategoria modificarSubCategoria(String id, String nombre, String idCategoria);
    void eliminarSubCategoria(String id);
    List<SubCategoria> listarSubCategoria(String idCategoria);
    List<SubCategoria> listarSubCategoriaActivo(String idCategoria);
}