package com.tienda.zero.repository;

import com.tienda.zero.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, String> {
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
    List<Categoria> findByEliminadoFalse();
}
