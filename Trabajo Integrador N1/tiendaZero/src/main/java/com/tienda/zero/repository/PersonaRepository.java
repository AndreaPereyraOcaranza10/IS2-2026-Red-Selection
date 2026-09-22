package com.tienda.zero.repository;

import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, String> {

    Optional<Persona> findByTipoDocumentoAndNumeroDocumento(TipoDocumento tipoDocumento, String numeroDocumento);

    List<Persona> findByEliminadoFalse();
}
