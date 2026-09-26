package com.tienda.zero.repository;

import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, String> {

    Optional<Empleado> findByTipoDocumentoAndNumeroDocumento(TipoDocumento tipoDocumento, String numeroDocumento);
    List<Empleado> findByEliminadoFalse();
}