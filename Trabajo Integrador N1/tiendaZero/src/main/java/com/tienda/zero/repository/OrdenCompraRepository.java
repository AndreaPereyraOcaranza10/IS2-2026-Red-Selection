package com.tienda.zero.repository;

import com.tienda.zero.model.OrdenCompra;
import com.tienda.zero.enums.EstadoOrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, String> {
    List<OrdenCompra> findByClienteUsuarioNombreUsuarioOrderByFechaDesc(String username);
    Optional<OrdenCompra> findByIdAndClienteUsuarioNombreUsuario(String id, String username);
    Optional<OrdenCompra> findByIdentificadorCompra(String identificadorCompra);
    List<OrdenCompra> findByEstadoOrdenCompraNotAndEliminadoFalseOrderByFechaDesc(EstadoOrdenCompra estado);
    Optional<OrdenCompra> findFirstByClienteUsuarioNombreUsuarioAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(String username, EstadoOrdenCompra estado);
    Optional<OrdenCompra> findFirstByEmpleadoUsuarioNombreUsuarioAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(String username, EstadoOrdenCompra estado);
    Optional<OrdenCompra> findFirstByPropietarioNombreUsuarioAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(String username, EstadoOrdenCompra estado);
    List<OrdenCompra> findByFechaBetweenAndEstadoOrdenCompraNotInAndEliminadoFalseOrderByFechaDesc(
            LocalDate desde, LocalDate hasta, Collection<EstadoOrdenCompra> estadosExcluidos);
}

