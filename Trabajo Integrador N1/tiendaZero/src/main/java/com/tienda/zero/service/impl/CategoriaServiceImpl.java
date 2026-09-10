package com.tienda.zero.service.impl;

import com.tienda.zero.model.Categoria;
import com.tienda.zero.repository.CategoriaRepository;
import com.tienda.zero.service.CategoriaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    // Inyección de dependencias por constructor
    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public Categoria crearCategoria(String nombre) {
        validar(nombre);
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }

    @Override
    public void validar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        categoriaRepository.findByNombreIgnoreCase(nombre).ifPresent(c -> {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        });
    }

    @Override
    public Categoria buscarCategoria(String id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + id));
    }

    @Override
    public Categoria buscarCategoriaPorNombre(String nombre) {
        return categoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + nombre));
    }

    @Override
    public Categoria modificarCategoria(String id, String nombre) {
        Categoria categoria = buscarCategoria(id);
        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }

    @Override
    public void eliminarCategoria(String id) {
        Categoria categoria = buscarCategoria(id);
        categoria.setEliminado(true);
        categoriaRepository.save(categoria);
    }

    @Override
    public List<Categoria> listarCategoria() {
        return categoriaRepository.findAll();
    }

    @Override
    public List<Categoria> listarCategoriaActivo() {
        return categoriaRepository.findByEliminadoFalse();
    }
}