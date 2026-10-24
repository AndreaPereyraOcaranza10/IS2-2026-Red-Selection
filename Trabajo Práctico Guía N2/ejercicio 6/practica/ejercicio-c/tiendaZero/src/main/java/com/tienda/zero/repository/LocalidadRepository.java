package com.tienda.zero.repository;

import com.tienda.zero.model.Localidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, String> {

    List<Localidad> findByDepartamentoId(String idDepartamento);

    List<Localidad> findByDepartamentoIdAndEliminadoFalse(String idDepartamento);

    Optional<Localidad> findByNombre(String nombre);

    Optional<Localidad> findByCodigoPostal(String codigoPostal);
}