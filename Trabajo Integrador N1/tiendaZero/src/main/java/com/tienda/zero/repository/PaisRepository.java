package com.tienda.zero.repository;

import com.tienda.zero.model.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaisRepository extends JpaRepository<Pais, String> {

    List<Pais> findByEliminadoFalse();

    Optional<Pais> findByNombre(String nombre);
}
