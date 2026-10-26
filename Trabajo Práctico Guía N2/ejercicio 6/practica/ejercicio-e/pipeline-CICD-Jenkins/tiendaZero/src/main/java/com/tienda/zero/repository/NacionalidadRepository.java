package com.tienda.zero.repository;

import com.tienda.zero.model.Nacionalidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NacionalidadRepository extends JpaRepository<Nacionalidad, String> {

    Optional<Nacionalidad> findByNombreIgnoreCase(String nombre);

    List<Nacionalidad> findByEliminadoFalse();
}
