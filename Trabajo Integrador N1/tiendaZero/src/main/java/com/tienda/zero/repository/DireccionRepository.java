package com.tienda.zero.repository;

import com.tienda.zero.model.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, String> {

    Optional<Direccion> findByCalleAndNumeracion(String calle, String numeracion);
}