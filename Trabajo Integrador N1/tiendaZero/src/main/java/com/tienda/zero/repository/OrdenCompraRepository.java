package com.tienda.zero.repository;

import com.tienda.zero.model.OrdenCompra;
import com.tienda.zero.enums.EstadoOrdenCompra;
import com.tienda.zero.enums.TipoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrdenCompra o where o.id = :id")
    Optional<OrdenCompra> buscarParaFacturar(@Param("id") String id);
    List<OrdenCompra> findByPropietarioNombreUsuarioAndFormaPagoAndEstadoOrdenCompraAndEliminadoFalse(
            String username, TipoPago formaPago, EstadoOrdenCompra estado);
    long countByFormaPagoAndEstadoOrdenCompraInAndEliminadoFalse(
            TipoPago formaPago,
            Collection<EstadoOrdenCompra> estados);
    long countByEliminadoFalseAndEstadoOrdenCompraNot(EstadoOrdenCompra estado);
    List<OrdenCompra> findByClienteUsuarioNombreUsuarioOrderByFechaDescFechaHoraCreacionDescIdDesc(String username);
    List<OrdenCompra> findByPropietarioNombreUsuarioOrderByFechaDescFechaHoraCreacionDescIdDesc(String username);
    List<OrdenCompra> findByPropietarioNombreUsuarioOrClienteUsuarioNombreUsuarioOrEmpleadoUsuarioNombreUsuarioOrderByFechaDescFechaHoraCreacionDescIdDesc(
            String propietario, String cliente, String empleado);
    Optional<OrdenCompra> findByIdAndPropietarioNombreUsuario(String id, String username);
    Optional<OrdenCompra> findByIdAndClienteUsuarioNombreUsuario(String id, String username);
    Optional<OrdenCompra> findByIdentificadorCompra(String identificadorCompra);
    List<OrdenCompra> findByEstadoOrdenCompraNotAndEliminadoFalseOrderByFechaDescFechaHoraCreacionDescIdDesc(EstadoOrdenCompra estado);
    Optional<OrdenCompra> findFirstByClienteUsuarioNombreUsuarioAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(String username, EstadoOrdenCompra estado);
    Optional<OrdenCompra> findFirstByEmpleadoUsuarioNombreUsuarioAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(String username, EstadoOrdenCompra estado);
    Optional<OrdenCompra> findFirstByPropietarioNombreUsuarioAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(String username, EstadoOrdenCompra estado);
    List<OrdenCompra> findByFechaBetweenAndEstadoOrdenCompraNotInAndEliminadoFalseOrderByFechaDescFechaHoraCreacionDescIdDesc(
            LocalDate desde, LocalDate hasta, Collection<EstadoOrdenCompra> estadosExcluidos);
}
