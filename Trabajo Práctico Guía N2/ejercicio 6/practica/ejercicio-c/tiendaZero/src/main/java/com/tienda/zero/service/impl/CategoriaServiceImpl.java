package com.tienda.zero.service.impl;

import com.tienda.zero.model.Categoria;
import com.tienda.zero.repository.CategoriaRepository;
import com.tienda.zero.repository.SubCategoriaRepository;
import com.tienda.zero.service.CategoriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    private final SubCategoriaRepository subCategoriaRepository;


    // Inyección de dependencias por constructor
    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, SubCategoriaRepository subCategoriaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.subCategoriaRepository = subCategoriaRepository;

    }

    @Override
    @Transactional
    public Categoria crearCategoria(String nombre) {
        validar(nombre);
        String nombreLimpio = nombre.trim();

        if (categoriaRepository.findByNombreIgnoreCase(nombreLimpio).isPresent()) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombreLimpio);
        categoria.setEliminado(false);
        return categoriaRepository.save(categoria);
    }

    @Override
    public void validar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
    }

    @Override
    public Categoria buscarCategoria(String id) {

        Optional<Categoria> resultado = categoriaRepository.findById(id);
        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("Categoría no encontrada: " + id);
        }
        return resultado.get();

    }

    @Override
    public Categoria buscarCategoriaPorNombre(String nombre) {

        Optional<Categoria> resultado = categoriaRepository.findByNombreIgnoreCase(nombre.trim());
        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("Categoría no encontrada: " + nombre);
        }
        return resultado.get();

    }

    @Override
    @Transactional
    public Categoria modificarCategoria(String id, String nombre) {
        validar(nombre);
        Categoria categoria = buscarCategoria(id);

        if (categoria.isEliminado()) {
            throw new IllegalArgumentException("No se puede modificar una categoría eliminada");
        }

        String nombreLimpio = nombre.trim();
        Optional<Categoria> existente = categoriaRepository.findByNombreIgnoreCase(nombreLimpio);
        if (existente.isPresent() && !existente.get().getId().equals(id)) {
            throw new IllegalArgumentException("Ya existe otra categoría con ese nombre");
        }

        categoria.setNombre(nombreLimpio);
        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public void eliminarCategoria(String id) {
        Categoria categoria = buscarCategoria(id);

        if (!subCategoriaRepository.findByCategoriaIdAndEliminadoFalse(id).isEmpty()) {
            throw new IllegalArgumentException(
                    "No se puede eliminar una categoría que tiene subcategorías activas. Eliminá primero sus subcategorías.");
        }

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