package com.tienda.zero.service;

import com.tienda.zero.model.Categoria;
import java.util.List;

public interface CategoriaService {
    Categoria crearCategoria(String nombre);
    void validar(String nombre);
    Categoria buscarCategoria(String id);
    Categoria buscarCategoriaPorNombre(String nombre);
    Categoria modificarCategoria(String id, String nombre);
    void eliminarCategoria(String id);
    List<Categoria> listarCategoria();
    List<Categoria> listarCategoriaActivo();
}