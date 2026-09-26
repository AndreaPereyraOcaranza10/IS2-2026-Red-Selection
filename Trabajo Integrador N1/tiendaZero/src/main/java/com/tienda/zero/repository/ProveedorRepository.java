package com.tienda.zero.repository;

import com.tienda.zero.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, String> {

    List<Proveedor> findByEliminadoFalse();
    Optional<Proveedor> findByRazonSocialIgnoreCaseAndEliminadoFalse(String razonSocial);
}