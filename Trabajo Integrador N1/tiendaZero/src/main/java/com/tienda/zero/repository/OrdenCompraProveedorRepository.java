package com.tienda.zero.repository;

import com.tienda.zero.model.OrdenCompraProveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenCompraProveedorRepository extends JpaRepository<OrdenCompraProveedor, String> {
    List<OrdenCompraProveedor> findByEntregadaFalseOrderByFechaCreacionDescFechaHoraCreacionDescIdDesc();
    List<OrdenCompraProveedor> findAllByOrderByFechaCreacionDescFechaHoraCreacionDescIdDesc();
}
