package com.tienda.zero.service.impl;

import com.tienda.zero.model.Categoria;
import com.tienda.zero.model.SubCategoria;
import com.tienda.zero.repository.SubCategoriaRepository;
import com.tienda.zero.service.CategoriaService;
import com.tienda.zero.service.SubCategoriaService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    @Transactional
    public SubCategoria crearSubCategoria(String nombre, String idCategoria) {

        validar(nombre, idCategoria);

        Categoria categoria = buscarCategoriaActiva(idCategoria);
        String nombreLimpio = nombre.trim();

        if (subCategoriaRepository.findByNombreIgnoreCaseAndCategoriaId(nombreLimpio, idCategoria).isPresent()) {
            throw new IllegalArgumentException("Ya existe una subcategoría con ese nombre en esta categoría");
        }

        SubCategoria subCategoria = new SubCategoria();
        subCategoria.setNombre(nombreLimpio);
        subCategoria.setCategoria(categoria);
        subCategoria.setEliminado(false);
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
    }

    @Override
    public SubCategoria buscarSubCategoria(String id) {

        Optional<SubCategoria> resultado = subCategoriaRepository.findById(id);
        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("Subcategoría no encontrada: " + id);
        }
        return resultado.get();

    }

    @Override
    public SubCategoria buscarSubCategoriaPorNombre(String nombre) {

        Optional<SubCategoria> resultado = subCategoriaRepository.findByNombreIgnoreCase(nombre.trim());
        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("Subcategoría no encontrada: " + nombre);
        }
        return resultado.get();

    }

    @Override
    public SubCategoria modificarSubCategoria(String id, String nombre, String idCategoria) {

        validar(nombre, idCategoria);
        SubCategoria subCategoria = buscarSubCategoria(id);

        if (subCategoria.isEliminado()) {
            throw new IllegalArgumentException("No se puede modificar una subcategoría eliminada");
        }

        Categoria categoria = buscarCategoriaActiva(idCategoria);
        String nombreLimpio = nombre.trim();

        Optional<SubCategoria> existente =
                subCategoriaRepository.findByNombreIgnoreCaseAndCategoriaId(nombreLimpio, idCategoria);
        if (existente.isPresent() && !existente.get().getId().equals(id)) {
            throw new IllegalArgumentException("Ya existe otra subcategoría con ese nombre en esta categoría");
        }

        subCategoria.setNombre(nombreLimpio);
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

    private Categoria buscarCategoriaActiva(String idCategoria) {
        Categoria categoria = categoriaService.buscarCategoria(idCategoria);
        if (categoria.isEliminado()) {
            throw new IllegalArgumentException("La categoría está eliminada");
        }
        return categoria;
    }

}