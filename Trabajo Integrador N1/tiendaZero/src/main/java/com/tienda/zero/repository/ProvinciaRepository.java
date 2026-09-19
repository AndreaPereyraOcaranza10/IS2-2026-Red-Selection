package com.tienda.zero.repository;

import com.tienda.zero.model.Provincia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProvinciaRepository extends JpaRepository<Provincia, String> {

    List<Provincia> findByPaisId(String idPais);

    List<Provincia> findByPaisIdAndEliminadoFalse(String idPais);

    Optional<Provincia> findByNombre(String nombre);
}