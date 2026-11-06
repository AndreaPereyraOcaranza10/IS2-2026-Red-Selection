package com.dulcemayor.repository;

import com.dulcemayor.model.Empresa;
import com.dulcemayor.enums.TipoEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, String> {

    List<Empresa> findByEliminadoFalse();

    List<Empresa> findByTipoEmpresaAndEliminadoFalseOrderByRazonSocialAsc(TipoEmpresa tipoEmpresa);

    boolean existsByCuit(String cuit);

    boolean existsByCuitAndIdNot(String cuit, String id);

    Optional<Empresa> findFirstByRazonSocialIgnoreCaseAndEliminadoFalse(String razonSocial);
}
