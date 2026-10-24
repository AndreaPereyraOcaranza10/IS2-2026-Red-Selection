package com.tienda.zero.repository;

import com.tienda.zero.enums.TipoCorreo;
import com.tienda.zero.model.ConfiguracionCorreoEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracionCorreoEmpresaRepository extends JpaRepository<ConfiguracionCorreoEmpresa, String> {

    List<ConfiguracionCorreoEmpresa> findByEliminadoFalse();
    List<ConfiguracionCorreoEmpresa> findByEmpresaIdAndEliminadoFalse(String idEmpresa);
    Optional<ConfiguracionCorreoEmpresa> findByEmpresaIdAndTipoCorreoAndEliminadoFalse(String idEmpresa, TipoCorreo tipoCorreo);
    Optional<ConfiguracionCorreoEmpresa> findFirstByTipoCorreoAndEliminadoFalse(TipoCorreo tipoCorreo);
}