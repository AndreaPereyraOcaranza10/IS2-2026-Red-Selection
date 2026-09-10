package com.tienda.zero.repository;

import com.tienda.zero.model.SubCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubCategoriaRepository extends JpaRepository<SubCategoria, String> {
    Optional<SubCategoria> findByNombreIgnoreCase(String nombre);
    List<SubCategoria> findByCategoriaId(String idCategoria);
    List<SubCategoria> findByCategoriaIdAndEliminadoFalse(String idCategoria);
}