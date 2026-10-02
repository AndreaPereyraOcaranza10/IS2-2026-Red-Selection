package com.tienda.zero.repository;

import com.tienda.zero.model.FacturaCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface FacturaClienteRepository extends JpaRepository<FacturaCliente, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FacturaCliente f where f.id = :id")
    Optional<FacturaCliente> buscarParaEnviar(@Param("id") String id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FacturaCliente f where f.ordenCompra.id = :ordenId")
    Optional<FacturaCliente> buscarParaActualizar(@Param("ordenId") String ordenId);
    List<FacturaCliente> findTop20ByCorreoPendienteTrueAndCorreoEnviadoFalseAndEliminadoFalseOrderByFechaFacturaAscNumeroFacturaAsc();
    Optional<FacturaCliente> findByOrdenCompraId(String ordenId);
    List<FacturaCliente> findByEliminadoFalseOrderByFechaFacturaDescNumeroFacturaDesc();
    List<FacturaCliente> findByOrdenCompraPropietarioNombreUsuarioAndEliminadoFalseOrderByFechaFacturaDescNumeroFacturaDesc(String username);
}
