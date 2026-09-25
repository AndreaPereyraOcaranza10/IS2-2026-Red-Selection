package com.tienda.zero.repository;

import com.tienda.zero.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, String> {

    List<Empresa> findByEliminadoFalse();

    boolean existsByCuit(String cuit);

    boolean existsByCuitAndIdNot(String cuit, String id);

    Optional<Empresa> findFirstByRazonSocialIgnoreCaseAndEliminadoFalse(String razonSocial);
}