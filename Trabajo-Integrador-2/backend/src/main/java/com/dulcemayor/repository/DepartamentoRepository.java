package com.dulcemayor.repository;

import com.dulcemayor.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, String> {

    List<Departamento> findByProvinciaId(String idProvincia);

    List<Departamento> findByProvinciaIdAndEliminadoFalse(String idProvincia);

    Optional<Departamento> findByNombre(String nombre);
    Optional<Departamento> findByNombreIgnoreCaseAndProvinciaId(String nombre, String provinciaId);
}
