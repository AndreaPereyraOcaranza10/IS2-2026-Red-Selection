package com.tienda.zero.repository;

import com.tienda.zero.model.OrdenCompra;
import com.tienda.zero.enums.EstadoOrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, String> {
    List<OrdenCompra> findByClienteUsuarioNombreUsuarioOrderByFechaDesc(String username);
    Optional<OrdenCompra> findByIdAndClienteUsuarioNombreUsuario(String id, String username);
    Optional<OrdenCompra> findFirstByClienteUsuarioNombreUsuarioAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(String username, EstadoOrdenCompra estado);
    Optional<OrdenCompra> findFirstByEmpleadoUsuarioNombreUsuarioAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(String username, EstadoOrdenCompra estado);
    Optional<OrdenCompra> findFirstByPropietarioNombreUsuarioAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(String username, EstadoOrdenCompra estado);
}
