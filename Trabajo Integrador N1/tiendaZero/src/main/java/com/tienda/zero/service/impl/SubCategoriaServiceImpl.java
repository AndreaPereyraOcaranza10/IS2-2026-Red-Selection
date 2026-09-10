package com.tienda.zero.service.impl;

import com.tienda.zero.model.Categoria;
import com.tienda.zero.model.SubCategoria;
import com.tienda.zero.repository.SubCategoriaRepository;
import com.tienda.zero.service.CategoriaService;
import com.tienda.zero.service.SubCategoriaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubCategoriaServiceImpl implements SubCategoriaService {

    private final SubCategoriaRepository subCategoriaRepository;
    private final CategoriaService categoriaService;

    public SubCategoriaServiceImpl(SubCategoriaRepository subCategoriaRepository,
                                   CategoriaService categoriaService) {
        this.subCategoriaRepository = subCategoriaRepository;
        this.categoriaService = categoriaService;
    }

    @Override
    public SubCategoria crearSubCategoria(String nombre, String idCategoria) {
        validar(nombre, idCategoria);
        Categoria categoria = categoriaService.buscarCategoria(idCategoria);
        SubCategoria subCategoria = new SubCategoria();
        subCategoria.setNombre(nombre);
        subCategoria.setCategoria(categoria);
        return subCategoriaRepository.save(subCategoria);
    }

    @Override
    public void validar(String nombre, String idCategoria) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la subcategoría es obligatorio");
        }
        if (idCategoria == null || idCategoria.isBlank()) {
            throw new IllegalArgumentException("La subcategoría debe pertenecer a una categoría");
        }
        categoriaService.buscarCategoria(idCategoria);
    }

    @Override
    public SubCategoria buscarSubCategoria(String id) {
        return subCategoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subcategoría no encontrada: " + id));
    }

    @Override
    public SubCategoria buscarSubCategoriaPorNombre(String nombre) {
        return subCategoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Subcategoría no encontrada: " + nombre));
    }

    @Override
    public SubCategoria modificarSubCategoria(String id, String nombre, String idCategoria) {
        SubCategoria subCategoria = buscarSubCategoria(id);
        Categoria categoria = categoriaService.buscarCategoria(idCategoria);
        subCategoria.setNombre(nombre);
        subCategoria.setCategoria(categoria);
        return subCategoriaRepository.save(subCategoria);
    }

    @Override
    public void eliminarSubCategoria(String id) {
        SubCategoria subCategoria = buscarSubCategoria(id);
        subCategoria.setEliminado(true);
        subCategoriaRepository.save(subCategoria);
    }

    @Override
    public List<SubCategoria> listarSubCategoria(String idCategoria) {
        return subCategoriaRepository.findByCategoriaId(idCategoria);
    }

    @Override
    public List<SubCategoria> listarSubCategoriaActivo(String idCategoria) {
        return subCategoriaRepository.findByCategoriaIdAndEliminadoFalse(idCategoria);
    }
}