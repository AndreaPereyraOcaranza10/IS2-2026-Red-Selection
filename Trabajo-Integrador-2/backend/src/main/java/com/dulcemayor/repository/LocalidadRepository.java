package com.dulcemayor.repository;

import com.dulcemayor.model.Localidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, String> {

    List<Localidad> findByDepartamentoId(String idDepartamento);

    List<Localidad> findByDepartamentoIdAndEliminadoFalse(String idDepartamento);

    Optional<Localidad> findByNombre(String nombre);
    Optional<Localidad> findByNombreIgnoreCaseAndDepartamentoId(String nombre, String departamentoId);

    Optional<Localidad> findByCodigoPostal(String codigoPostal);
}
